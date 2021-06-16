package algorithms;

import app.Application;
import datatypes.Tabular;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

public class LDiversityAlgorithm {

    private static NumberFormat formatter = new DecimalFormat("#0.00");

    public static Map<String, Tabular[]> analyse(
            String datasetID,
            List<String> QIs,
            List<String> sensitiveAttributes,
            int l
    ) throws Exception {
        Connection c = DriverManager.getConnection(Application.dbPath);
        c.setAutoCommit(false);

        int datasetSize = getDatasetSize(c, datasetID);

        List<String> QIsCombinations = createQIsCombinations(QIs, ";");

        Map<String, Map<String, Double>> sensitiveAttributesCombinationsRisk = new HashMap<>();
        for (String sensitiveAttribute : sensitiveAttributes) {
            sensitiveAttributesCombinationsRisk.put(sensitiveAttribute, new HashMap<>());

            IntStream.range(0, QIsCombinations.size()).parallel().forEach(combinationsIndex -> {
                try {
                    Set<String> unsafe = new HashSet<>();

                    String combination = QIsCombinations.get(combinationsIndex);

                    String fields[] = combination.split(";");

                    List<String> stack = createStack(datasetSize);

                    while (!stack.isEmpty()) {
                        String valuesOf[] = getValuesOf(c, datasetID, fields, stack.get(0));

                        Map<String, String> entryIDSensitiveAttribute = getEntryIDSensitiveAttribute(c, datasetID, valuesOf, fields, sensitiveAttribute);

                        Set<String> entryIDs = entryIDSensitiveAttribute.keySet();

                        int count = (int) entryIDSensitiveAttribute.values().stream().distinct().count();

                        if (count < l) {
                            unsafe.addAll(entryIDs);
                        }

                        stack.removeAll(entryIDs);
                    }

                    double risk = (unsafe.size() / (double) datasetSize) * 100;
                    String formattedRisk = formatter.format(risk).replaceAll(",", ".");
                    risk = Double.parseDouble(formattedRisk);

                    Map<String, Double> temp = sensitiveAttributesCombinationsRisk.get(sensitiveAttribute);

                    temp.put(combination, risk);

                    sensitiveAttributesCombinationsRisk.put(sensitiveAttribute, temp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        c.commit();
        c.close();

        Map<String, Tabular[]> response = new HashMap<>();

        for (Map.Entry<String, Map<String, Double>> entry : sensitiveAttributesCombinationsRisk.entrySet()) {
            String sensitiveAttribute = entry.getKey();
            Tabular output[] = new Tabular[QIsCombinations.size()];
            int index = 0;
            for (Map.Entry<String, Double> entry2 : sensitiveAttributesCombinationsRisk.get(sensitiveAttribute).entrySet()) {
                String combination = entry2.getKey();
                Double risk = entry2.getValue();
                int numberOfQIs = combination.split(";").length;
                
                output[index++] = new Tabular(combination, numberOfQIs, risk);
            }
            
            response.put(sensitiveAttribute, output);
        }

        return response;
    }

    private static int getDatasetSize(Connection c, String datasetID) throws Exception {
        int datasetSize = 0;

        Statement stmt = c.createStatement();

        String sql = "SELECT seq FROM sqlite_sequence WHERE name = \"" + datasetID + "\"";

        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            datasetSize = Integer.parseInt(rs.getString("seq"));
        }

        rs.close();
        stmt.close();

        return datasetSize;
    }

    private static List<String> createQIsCombinations(
            List<String> QIs,
            String separator
    ) {
        String sequence[] = new String[QIs.size()];
        for (int i = 0; i < QIs.size(); i++) {
            sequence[i] = QIs.get(i);
        }

        List<String> combinations = new ArrayList<>();

        String[] data = new String[QIs.size()];

        for (int r = 0; r < sequence.length; r++) {
            combinations(sequence, data, 0, QIs.size() - 1, 0, r, combinations, separator);
        }

        String all = "";
        all = QIs.stream().map((QI) -> QI + separator).reduce(all, String::concat);
        all = all.substring(0, all.length() - 1);

        combinations.add(all);

        return combinations;
    }

    private static void combinations(
            String[] sequence,
            String[] data,
            int start,
            int end,
            int index,
            int r,
            List<String> combinations,
            String separator
    ) {
        if (index == r) {
            String combination = "";

            for (int j = 0; j < r; j++) {
                combination += data[j] + separator;
            }

            if (!combination.equals("")) {
                combination = combination.substring(0, combination.length() - 1);
                combinations.add(combination);
            }
        }

        for (int i = start; i <= end && ((end - i + 1) >= (r - index)); i++) {
            data[index] = sequence[i];
            combinations(sequence, data, i + 1, end, index + 1, r, combinations, separator);
        }
    }

    private static List<String> createStack(int datasetSize) {
        List<String> stack = new ArrayList<>();

        for (int i = 1; i <= datasetSize; i++) {
            stack.add(i + "");
        }

        return stack;
    }

    private static String[] getValuesOf(Connection c, String datasetID, String fields[], String entryID) throws Exception {
        String valuesOf[] = new String[fields.length];

        Statement stmt = c.createStatement();

        String sql = "SELECT " + createSelectFields(fields)
                + " FROM " + datasetID
                + " WHERE EntryID = " + entryID;

        ResultSet rs = stmt.executeQuery(sql);

        if (rs.next()) {
            for (int i = 0; i < fields.length; i++) {
                valuesOf[i] = rs.getString(fields[i]);
            }
        }

        rs.close();
        stmt.close();

        return valuesOf;
    }

    private static String createSelectFields(String fields[]) {
        String select = "";
        for (String field : fields) {
            select += "[" + field + "],";
        }
        select = select.substring(0, select.lastIndexOf(","));

        return select;
    }

    private static Map<String, String> getEntryIDSensitiveAttribute(Connection c, String datasetID, String valuesOf[], String fields[], String sensitiveAttribute) throws Exception {
        Map<String, String> entryIDSensitiveAttribute = new HashMap<>();

        Statement stmt = c.createStatement();

        String sql = "SELECT EntryID,[" + sensitiveAttribute + "]"
                + " FROM " + datasetID
                + " WHERE " + createWhere(fields, valuesOf) + ";";

        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            entryIDSensitiveAttribute.put(rs.getInt("EntryID") + "", rs.getString(sensitiveAttribute));
        }

        rs.close();
        stmt.close();

        return entryIDSensitiveAttribute;
    }

    private static String createWhere(String fields[], String valuesOf[]) {
        String where = "";

        for (int i = 0; i < fields.length; i++) {
            where += "[" + fields[i] + "] = \"" + valuesOf[i] + "\" AND ";
        }
        where = where.substring(0, where.lastIndexOf(" AND"));

        return where;
    }
}
