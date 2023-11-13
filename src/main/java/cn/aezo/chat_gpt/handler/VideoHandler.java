package cn.aezo.chat_gpt.handler;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoHandler {
    public Map<String, Object> douyin(String url) {
        try {
            String pattern2 = "https://v\\.douyin\\.com/\\S+"; // 正则表达式匹配
            Pattern urlPattern = Pattern.compile(pattern2);
            Matcher matcher2 = urlPattern.matcher(url);
            if (matcher2.find()) {
                url = matcher2.group(); // 提取匹配到的 URL 部分
            }
            URL originalUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) originalUrl.openConnection();
            connection.setInstanceFollowRedirects(false);
            String loc = connection.getHeaderField("Location");
            String id = "";
            // 定义一个正则表达式
            String regex = "/video/(\\d+?)/"; // 匹配一个或多个数字
            // 创建 Pattern 对象
            Pattern pattern = Pattern.compile(regex);
            // 创建 matcher 对象
            Matcher matcher = pattern.matcher(loc);
            // 使用 matcher 对象查找匹配
            if (matcher.find()) {
                id = matcher.group(1);
            } else {
                System.out.println("No match found.");
            }
            String apiUrl = "https://tiktok.iculture.cc/X-Bogus";
            Map<String, String> data = new HashMap<>();
            data.put("url", "https://www.douyin.com/aweme/v1/web/aweme/detail/?aweme_id=" + id +
                    "&aid=1128&version_name=23.5.0&device_platform=android&os_version=2333");
            data.put("user_agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
            String jsonInputString = new JSONObject(data).toString();
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            String responseData = curl(apiUrl, headers, jsonInputString);
            String videoUrl = new JSONObject(responseData).getString("param");
            headers.clear();  // Clear headers for the next request
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
            headers.put("Referer", "https://www.douyin.com/");
            headers.put("Cookie", "ttwid=1%7CC2GvTdgBefgzOqCOkC6P1x6st6lP9mYooez2segMrNw%7C1699863155%7C749b142066b1fcd0e998eb7a1c40b2b6b144f7bb942cfe8822c63f602336b10d; douyin.com; device_web_cpu_core=12; device_web_memory_size=8; architecture=amd64; webcast_local_quality=null; home_can_add_dy_2_desktop=%220%22; stream_recommend_feed_params=%22%7B%5C%22cookie_enabled%5C%22%3Atrue%2C%5C%22screen_width%5C%22%3A1920%2C%5C%22screen_height%5C%22%3A1080%2C%5C%22browser_online%5C%22%3Atrue%2C%5C%22cpu_core_num%5C%22%3A12%2C%5C%22device_memory%5C%22%3A8%2C%5C%22downlink%5C%22%3A10%2C%5C%22effective_type%5C%22%3A%5C%224g%5C%22%2C%5C%22round_trip_time%5C%22%3A150%7D%22; passport_csrf_token=a0e86cdf2e0a6aae814cb6e960703ebb; passport_csrf_token_default=a0e86cdf2e0a6aae814cb6e960703ebb; FORCE_LOGIN=%7B%22videoConsumedRemainSeconds%22%3A180%7D; strategyABtestKey=%221699863158.699%22; s_v_web_id=verify_lowmmvp7_tY29GafT_KjEO_4vyE_92WX_f5PkbKdBJnJv; ttcid=2d8a46c147c14c0fa3ae115f7c6d340725; tt_scid=Iw.7YqqeCbKPd1xLv9.qlKxuoKkDpk2ZheD0GccT2sG41I9PyCbuzDqP2OiAy5OK579a; passport_assist_user=CjxH4lu3HLZatOST5rHkOc2O308yK7OP8KdLAoZkeKmfkE516L9W1_GvhTSY_4NrtLb-D95vbWAR8iCrphQaSgo8YbpRJjFtIViN47YxQIi-Zsa5eqJlFf9PK0RJY_rikfZl3ekgq4OXmKpcTeOUwft2mAvTlwkXGw4NkxglEPWVwQ0Yia_WVCABIgED2OiGMw%3D%3D; n_mh=gsxc4bW8jRluV_mEWU1obfgEOJ4ySQSkumyEAXqk-uw; sso_uid_tt=cd84368e62c7a2c0a81ff6fd8d3f3328; sso_uid_tt_ss=cd84368e62c7a2c0a81ff6fd8d3f3328; toutiao_sso_user=5a2e09352d8b7aa8212f6ca9b330dbf7; toutiao_sso_user_ss=5a2e09352d8b7aa8212f6ca9b330dbf7; sid_ucp_sso_v1=1.0.0-KDVjMTQ4Y2E0YTUwOTUwZjBjYTkxYTkzM2M3NzdlNzE1ZDU0NmNmMWUKHQiGxePFhQIQjLXHqgYY7zEgDDCIrZ_OBTgGQPQHGgJscSIgNWEyZTA5MzUyZDhiN2FhODIxMmY2Y2E5YjMzMGRiZjc; ssid_ucp_sso_v1=1.0.0-KDVjMTQ4Y2E0YTUwOTUwZjBjYTkxYTkzM2M3NzdlNzE1ZDU0NmNmMWUKHQiGxePFhQIQjLXHqgYY7zEgDDCIrZ_OBTgGQPQHGgJscSIgNWEyZTA5MzUyZDhiN2FhODIxMmY2Y2E5YjMzMGRiZjc; passport_auth_status=61d932035288827442851eae65593f0f%2C; passport_auth_status_ss=61d932035288827442851eae65593f0f%2C; uid_tt=79e32d8ddac75124101898e5ce4781b6; uid_tt_ss=79e32d8ddac75124101898e5ce4781b6; sid_tt=6d8c508d47565129bdd4b3b8b18ecfdd; sessionid=6d8c508d47565129bdd4b3b8b18ecfdd; sessionid_ss=6d8c508d47565129bdd4b3b8b18ecfdd; __ac_nonce=06551da8e00500b2c49d2; IsDouyinActive=true; LOGIN_STATUS=1; FOLLOW_LIVE_POINT_INFO=%22MS4wLjABAAAA66vsbXS7M9J2cx-AIUARQflDSM5ZcXP-MZtsa7YHGSc%2F1699891200000%2F0%2F1699863184432%2F0%22; passport_fe_beating_status=true; _bd_ticket_crypt_doamin=2; _bd_ticket_crypt_cookie=6fd26d6371e07c2f57b7016ff711cb5e; msToken=6JvO8Y_suRZuL8p8xkxUsoxvKrtRxyXA5d6xvX-JvGh4xq549qNp_7KkJCuHu7oYWcV3x5Vzz8g5iuIEWwpMzgxUhiXV9yXUe4_6dx7NPiFfL6HsJrK_o-u4ZcA=; __security_server_data_status=1; bd_ticket_guard_client_data=eyJiZC10aWNrZXQtZ3VhcmQtdmVyc2lvbiI6MiwiYmQtdGlja2V0LWd1YXJkLWl0ZXJhdGlvbi12ZXJzaW9uIjoxLCJiZC10aWNrZXQtZ3VhcmQtcmVlLXB1YmxpYy1rZXkiOiJCSmJPZ0hSczd0S2FHQnJVajFGdlJqTEJBMWZDOXlQTjI4eDdpaEgzL0dDRXNyaDRFeE8rblFsV1JNeWd6bFJ0bEg2RmxPUnliUUxwSWE1dHduM3dYOFU9IiwiYmQtdGlja2V0LWd1YXJkLXdlYi12ZXJzaW9uIjoxfQ%3D%3D; sid_guard=6d8c508d47565129bdd4b3b8b18ecfdd%7C1699863186%7C5183997%7CFri%2C+12-Jan-2024+08%3A13%3A03+GMT; sid_ucp_v1=1.0.0-KDUxNmZjNTRiMDRmOGJlNDJjYzgyMGIwNTJhZmNlYTIwZThmMGMxZDEKGQiGxePFhQIQkrXHqgYY7zEgDDgGQPQHSAQaAmxmIiA2ZDhjNTA4ZDQ3NTY1MTI5YmRkNGIzYjhiMThlY2ZkZA; ssid_ucp_v1=1.0.0-KDUxNmZjNTRiMDRmOGJlNDJjYzgyMGIwNTJhZmNlYTIwZThmMGMxZDEKGQiGxePFhQIQkrXHqgYY7zEgDDgGQPQHSAQaAmxmIiA2ZDhjNTA4ZDQ3NTY1MTI5YmRkNGIzYjhiMThlY2ZkZA; store-region=cn-js; store-region-src=uid; publish_badge_show_info=%220%2C0%2C0%2C1699863189291%22; msToken=1QQ3kDlN7E4CPk_CiRqByNdh9_wh6ER-TzmUNY9UHqcKmDw9pNtAf-P65Lmufv-F8uWMkatEG67m_EpgD87MPXJ9bKFOG1WsxcX_ezqkXRABf8VcWnj57Yo4mOA=; odin_tt=9b273626d8fb8350af3d3d3c3e5530a59f07dd94da78bb9382efe655181363c866bea6eae627d84313231f1bd50cddc7f561119c229f25bfa3671899d4c39e97");
            String response = curl(videoUrl, headers);
            Map<String, Object> videoData = new Gson().fromJson(response, Map.class);
            Object awemeDetailObj = videoData.get("aweme_detail");
            if (awemeDetailObj instanceof Map) {
                Map<String, Object> awemeDetail = (Map<String, Object>) awemeDetailObj;
                Object videoObj = awemeDetail.get("video");
                if (videoObj instanceof Map) {
                    Map<String, Object> video = (Map<String, Object>) videoObj;
                    Object playAddrObj = video.get("play_addr");
                    if (playAddrObj instanceof Map) {
                        Map<String, Object> playAddr = (Map<String, Object>) playAddrObj;
                        Object urlListObj = playAddr.get("url_list");
                        if (urlListObj instanceof List) {
                            List<String> urlList = (List<String>) urlListObj;
                            if (!urlList.isEmpty()) {
                                videoUrl = urlList.get(0);
                            }
                        }
                    }
                }
            }
            if (videoUrl.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("code", 201);
                errorResponse.put("msg", "解析失败");
                return errorResponse;
            }
            Map<String, Object> responseDataMap = new HashMap<>();
            Map<String, Object> awemeDetail = (Map<String, Object>) videoData.get("aweme_detail");
            responseDataMap.put("code", 200);
            responseDataMap.put("msg", "解析成功");
            String finalVideoUrl = videoUrl;
            responseDataMap.put("data", new HashMap<String, Object>() {{
                put("title", awemeDetail.get("desc"));
                put("url", finalVideoUrl);
            }});
            return responseDataMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String curl(String url, Map<String, String> headers, String data) throws Exception {
        URL apiUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("POST");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        connection.setDoOutput(true);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = data.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    public Map<String, Object> pipixia(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setInstanceFollowRedirects(false);
            String loc = connection.getHeaderField("Location");

            Pattern pattern = Pattern.compile("item/(.*)\\?");
            Matcher matcher = pattern.matcher(loc);
            String id = "";
            if (matcher.find()) {
                id = matcher.group(1);
            }
            String apiUrl = "https://is.snssdk.com/bds/cell/detail/?cell_type=1&aid=1319&app_name=super&cell_id=" + id;
            String apiResponse = curl(apiUrl);
            Map<String, Object> result = new HashMap<>();
            if (apiResponse != null) {
                Map<String, Object> videoData = new Gson().fromJson(apiResponse, Map.class);
//                String videoUrl = (String) apiData.get("video_url");
//
//                if (videoUrl != null && !videoUrl.isEmpty()) {
//                    result.put("code", 200);
//                    result.put("data", apiData);
//                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Map<String, Object> bilibili(String url) {
        Pattern pattern1 = Pattern.compile("/id=(.*)\\b/");
        Matcher matcher1 = pattern1.matcher(url);
        // 如果找到匹配项，则返回匹配到的视频链接
        if (matcher1.find()) {
            url =  "https://bbq.bilibili.com/bbq/app-bbq/sv/detail?svid=" + matcher1.group(1);
        }
        String curl = curl(url);
        System.out.println("");
        return null;
    }

    public Map<String, Object> kuaishou(String url) {
        try {
            Pattern pattern1 = Pattern.compile("https://v\\.kuaishou\\.com/([\\w\\d]+)");
            Matcher matcher1 = pattern1.matcher(url);

            // 如果找到匹配项，则返回匹配到的视频链接
            if (matcher1.find()) {
                url =  "https://v.kuaishou.com/" + matcher1.group(1);
            }
            String videoId;
            String redirectUrl = "";
            if (url.contains("v.kuaishou.com")) {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setInstanceFollowRedirects(false);
                    redirectUrl = connection.getHeaderField("Location");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Pattern pattern = Pattern.compile("photoId=(.*?)\\&");
                Matcher matcher = pattern.matcher(redirectUrl);
                videoId = matcher.find() ? matcher.group(1) : "";
            } else {
                Pattern pattern = Pattern.compile("short-video\\/(.*?)\\?");
                Matcher matcher = pattern.matcher(url);
                videoId = matcher.find() ? matcher.group(1) : "";
            }
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Cookie", "_did=web_483188962CB77A82; userId=56886452; didv=1699885450027; did=web_9f179ae15842d6180550939ef737b6e3; userId=56886452; passToken=ChNwYXNzcG9ydC5wYXNzLXRva2VuEsABDmdmhhbJd3z9sKzz5DL0hXGyZ_PLJJdGbQZfLEcWutSGtZdjiDfFON-JeLhhLW-Yblmx3reAta9WDFbpkJsey0iG_R1D7LTKPx1xfXehNFntg3sRsUANRArnPc_d5wHqR3cFP6fZ4Kl1dp7n1qs9sIaKyLglQn8vaMobr-mt7GiQjkMaIfGoxPBMwKbm72as974CYcV9sbonC8B5-83_5cI8AsoYeZEPt-N2r6e3K093B2KD2ENCUk4xVIEUsV5IGhK_ktDxJOBCabJNg9UTt69gczsiIDehGAUo9YA1nxo80vfrT06f1sPiDrW30IqrPKg5iEdnKAUwAQ");
            headers.put("Referer", redirectUrl);
            headers.put("Content-Type", "application/json");
            String post_data = "{\"photoId\": \"" + videoId + "\",\"isLongVideo\": false}";
            String apiUrl = "https://v.m.chenzhongtech.com/rest/wd/photo/info";
            String jsonResponse = curl(apiUrl, headers, post_data);
            Map<String, Object> result = new HashMap<>();
            if (jsonResponse != null) {
                Map<String, Object> videoData = new Gson().fromJson(jsonResponse, Map.class);
                if (videoData != null && !videoData.isEmpty()) {
                    result.put("code", 200);
                    result.put("msg", "解析成功");
                    LinkedTreeMap photo = (LinkedTreeMap) videoData.get("photo");
                    String title = photo.get("caption") +"";
                    ArrayList mainMvUrls = (ArrayList) photo.get("mainMvUrls");
                    LinkedTreeMap hashMap = (LinkedTreeMap) mainMvUrls.get(0);
                    String videoUrls = hashMap.get("url")+"";
                    HashMap<String,Object> data = new HashMap<>();
                    data.put("title",title);
                    data.put("url",videoUrls);
                    result.put("data",data);
                }else {
                    result.put("code", 400);
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }


    private String curl(String apiUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String curl(String url, Map<String, String> headers) throws Exception {
        URL apiUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

        connection.setRequestMethod("GET");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }


}
