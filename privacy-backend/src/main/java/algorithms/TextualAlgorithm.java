package algorithms;

import app.Application;
import datatypes.Textual;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TextualAlgorithm {

    public static Textual analyse(
            String datasetID,
            String userIDColumn,
            String textColumn
    ) throws Exception {
        Map<String, Set<String>> userIDTextsSet = new HashMap<>();

        Connection c = DriverManager.getConnection(Application.dbPath);
        c.setAutoCommit(false);

        Statement stmt = c.createStatement();

        String sql = "SELECT [" + userIDColumn + "], [" + textColumn + "] FROM `" + datasetID + "`";

        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            String userID = rs.getString(userIDColumn);
            String text = rs.getString(textColumn);

            Set<String> temp = new HashSet<>();
            if (userIDTextsSet.containsKey(userID)) {
                temp = userIDTextsSet.get(userID);
            }
            temp.add(text);

            userIDTextsSet.put(userID, temp);
        }

        stmt.close();
        c.commit();
        c.close();

        Map<String, String> userIDText = createMergedTextPerUser(userIDTextsSet);

        String users[] = userIDText.keySet().toArray(new String[userIDText.keySet().size()]);
        double similarity[][] = new double[users.length][users.length];

        for (int i = 0; i < users.length; i++) {
            String user1Text = userIDText.get(users[i]);
            for (int j = i; j < users.length; j++) {
                String user2Text = userIDText.get(users[j]);

                double jaccardSimilarity = computeJaccard(user1Text, user2Text);

                similarity[i][j] = jaccardSimilarity;
                similarity[j][i] = jaccardSimilarity;
            }
        }

        Textual output = new Textual(users, users, similarity);

        return output;
    }

    private static Map<String, String> createMergedTextPerUser(Map<String, Set<String>> userIDTextsSet) {
        Map<String, String> userIDText = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry : userIDTextsSet.entrySet()) {
            String userID = entry.getKey();
            Set<String> texts = entry.getValue();

            String mergedText = "";
            for (String text : texts) {
                mergedText += text + " ";
            }
            mergedText = mergedText.substring(0, mergedText.length() - 1);

            userIDText.put(userID, mergedText);
        }

        return userIDText;
    }

    private static double computeJaccard(String text1, String text2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(text1.split(" ")));
        Set<String> set2 = new HashSet<>(Arrays.asList(text2.split(" ")));

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return ((double) intersection.size() / union.size());
    }
}
