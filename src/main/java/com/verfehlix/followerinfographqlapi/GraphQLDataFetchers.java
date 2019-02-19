package com.verfehlix.followerinfographqlapi;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import graphql.schema.DataFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class GraphQLDataFetchers {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * datafetcher for the follower_info table
     * - executes an SQL query to fetch data from the follower_info table
     * - fetches the last 100 follower_info entries
     * - startTimestamp and endTimestamp are optional graphql parameters
     *   (if they are used, the query will limit the results via the timestamp)
     * @return graphql datafetcher object that can be used by a wiring
     */
    DataFetcher getFollowerInfoDataFetcher() {
        return dataFetchingEnvironment -> {

            // get possible arguments from graphql query
            String startTimestamp = dataFetchingEnvironment.getArgument("startTimestamp");
            String endTimestamp = dataFetchingEnvironment.getArgument("endTimestamp");

            // start / end timestamps are available --> fetch all follower_info entries in that time range
            if(startTimestamp != null && endTimestamp != null) {
                // SQL query
                String sqlQuery =
                                "SELECT * \n" +
                                "FROM follower_info \n" +
                                "WHERE timestamp <= :endTimestamp \n" +
                                "AND timestamp >= :startTimestamp \n" +
                                "ORDER BY id DESC ";

                // defined named parameters for query
                SqlParameterSource namedParameters = new MapSqlParameterSource()
                        .addValue("startTimestamp", getTimeStampFromDateString(startTimestamp))
                        .addValue("endTimestamp", getTimeStampFromDateString(endTimestamp));

                // Execute query
                List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(
                        sqlQuery,
                        namedParameters
                );

                // Convert returned rows to list & return result
                return convertRowsToFollowerInfoList(rows);
            }
            // NO start / end timestamps provided --> fetch last 100 follower_info entries
            else {
                // SQL query
                String sqlQuery =
                                "SELECT * \n" +
                                "FROM follower_info \n" +
                                "ORDER BY id DESC LIMIT 100";

                // Execute query
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(sqlQuery);

                // Convert returned rows to list & return result
                return convertRowsToFollowerInfoList(rows);
            }
        };
    }

    /**
     * Converts a date-string (in format yyyy-MM-dd hh:mm:ss) into a java.sql.Timestamp object
     * @param dateString
     * @return a java.sql.Timestamp object based on the given date-string
     * @throws ParseException
     */
    private Timestamp getTimeStampFromDateString(String dateString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date parsedTimeStamp = dateFormat.parse(dateString);
        return new Timestamp(parsedTimeStamp.getTime());
    }

    /**
     * converts a list of rows (jdbcTemplate) fetched from the follower_info table into a
     * list that can be used & returned by the graphql datafetcher
     * @param rows list of rows object as returned by jdbcTemplate's queryForList
     * @return a list of follower-info maps
     */
    private List<Map<String, Object>> convertRowsToFollowerInfoList(List<Map<String, Object>> rows) {

        // create empty followerInfoList that will contain all follower infos returned from the DB
        List<Map<String, Object>> followerInfoList = new ArrayList<Map<String, Object>>();

        // iterate rows
        for (Map row : rows) {

            // create empty hashmap that will contain follower info of one row
            Map<String, Object> adaptedFollowerInfo = new HashMap<String, Object>();

            // fill hashmap with id, timestamp and follower count
            adaptedFollowerInfo.put("id", row.get("id"));
            adaptedFollowerInfo.put("timestamp", row.get("timestamp"));
            adaptedFollowerInfo.put("followerCount", row.get("follower_count"));

            // fill hashmap with follower list (convert json-string -> json object -> list)
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> followerList = gson.fromJson(row.get("follower_list").toString(), listType);
            adaptedFollowerInfo.put("followerList", followerList);

            // add follower info to list of all follower infos
            followerInfoList.add(adaptedFollowerInfo);
        }

        return followerInfoList;
    }
}
