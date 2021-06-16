package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

public class LDiversityWithOneConnectionParallelised {

    private static String datasetID = "dgqergdeia";
    private static List<String> QIs = Arrays.asList(new String[]{
        "sex", "race", "education", "age"
    });
    private static List<String> sensitiveAttributes = Arrays.asList(new String[]{
        "salary-class"
    });
    private static int l = 2;

    private static NumberFormat formatter = new DecimalFormat("#0.00");

    public static void main(String args[]) throws Exception {
        Connection c = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\alexb\\Desktop\\work\\trusts\\anonymisation_de-anonymisation_risk_analysis\\user_datasets\\db.db");
        c.setAutoCommit(false);

        int datasetSize = getDatasetSize(c, datasetID);

        List<String> QIsCombinations = createQIsCombinations(QIs, "\t");

        Map<String, Map<String, Double>> sensitiveAttributesCombinationsRisk = new HashMap<>();
        for (String sensitiveAttribute : sensitiveAttributes) {
            sensitiveAttributesCombinationsRisk.put(sensitiveAttribute, new HashMap<>());

            IntStream.range(0, QIsCombinations.size()).parallel().forEach(combinationsIndex -> {
                try {
                    List<String> unsafe = new ArrayList<>();

                    String combination = QIsCombinations.get(combinationsIndex);

                    String fields[] = combination.split("\t");

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
                    System.out.println(combination + "\t" + risk);
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

    private static String addFullSetOfQIs(List<String> QIs, String separator) {
        String combination = "";

        for (String QI : QIs) {
            combination += QI + separator;
        }
        combination = combination.substring(0, combination.lastIndexOf("\t"));

        return combination;
    }

    private static Map<Integer, List<String>> getNumberOfQIsQIsCombinations(
            List<String> QIsCombinations,
            String separator
    ) {
        Map<Integer, List<String>> numberOfQIsQIsCombinations = new HashMap<>();

        QIsCombinations.forEach((QIsCombination) -> {
            int numberOfQIs = QIsCombination.split(separator).length;

            List<String> temp;
            if (numberOfQIsQIsCombinations.containsKey(numberOfQIs)) {
                temp = numberOfQIsQIsCombinations.get(numberOfQIs);
            } else {
                temp = new ArrayList<>();
            }
            temp.add(QIsCombination);

            numberOfQIsQIsCombinations.put(numberOfQIs, temp);
        });

        return numberOfQIsQIsCombinations;
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
