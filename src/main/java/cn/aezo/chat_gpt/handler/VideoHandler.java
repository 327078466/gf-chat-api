package cn.aezo.chat_gpt.handler;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.hutool.crypto.SecureUtil.md5;
import static org.apache.commons.lang3.StringUtils.trim;

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
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
    }


    public Map<String, Object> pipixia(String url) {
        try {
            URL targetUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
            connection.setRequestMethod("HEAD");
            String loc = connection.getHeaderField("Location");
            String id = "";
            if (loc != null) {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("item/(.*)\\?");
                java.util.regex.Matcher matcher = pattern.matcher(loc);
                if (matcher.find()) {
                    id = matcher.group(1);
                }
            }
            String apiUrl = "https://is.snssdk.com/bds/cell/detail/?cell_type=1&aid=1319&app_name=super&cell_id=" + id;
            String response = curl(apiUrl);
            JSONObject json = new JSONObject(response);
            JSONObject data = json.getJSONObject("data").getJSONObject("data").getJSONObject("item");
            String videoUrl = data.getJSONObject("origin_video_download")
                    .getJSONArray("url_list")
                    .getJSONObject(0)
                    .getString("url");
            if (videoUrl != null && !videoUrl.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("data", new JSONObject()
                        .put("author", data.getJSONObject("author").getString("name"))
                        .put("avatar", data.getJSONObject("author").getJSONObject("avatar").getJSONArray("download_list").getJSONObject(0).getString("url"))
                        .put("time", json.getJSONObject("data").getJSONObject("data").getString("display_time"))
                        .put("title", data.getString("content"))
                        .put("cover", data.getJSONObject("cover").getJSONArray("url_list").getJSONObject(0).getString("url"))
                        .put("url", videoUrl));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> huoshan(String url) {
        try {
            URL targetUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
            connection.setRequestMethod("HEAD");
            String loc = connection.getHeaderField("Location");
            String id = "";
            Pattern pattern;
            if (loc != null) {
                pattern = Pattern.compile("item_id=(.*)&tag");
                Matcher matcher = pattern.matcher(loc);
                if (matcher.find()) {
                    id = matcher.group(1);
                }
            }
            String apiUrl = "https://share.huoshan.com/api/item/info?item_id=" + id;
            String response = curl(apiUrl);
            JSONObject json = new JSONObject(response);
            JSONObject data = json.getJSONObject("data").getJSONObject("item_info");
            String videoUrl = data.getString("url");
            pattern = Pattern.compile("video_id=(.*)&line");
            Matcher matcher = pattern.matcher(videoUrl);
            if (matcher.find()) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("msg", "解析成功");
                result.put("data", new JSONObject()
                        .put("cover", data.getString("cover")).put("title", "")
                        .put("url", "https://api-hl.huoshan.com/hotsoon/item/video/_playback/?video_id=" + matcher.group(1)));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }
    public Map<String, Object> weishi(String url) {
        try {
            String id = "";
            Pattern pattern = Pattern.compile("feed/(.*)\\b");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                id = matcher.group(1);
            }
            String apiUrl = "";
            if (url.contains("h5.weishi")) {
                apiUrl = "https://h5.weishi.qq.com/webapp/json/weishi/WSH5GetPlayPage?feedid=" + id;
            } else {
                apiUrl = "https://h5.weishi.qq.com/webapp/json/weishi/WSH5GetPlayPage?feedid=" + url;
            }

            String response = curl(apiUrl);

            JSONObject json = new JSONObject(response);
            JSONObject data = json.getJSONObject("data").getJSONArray("feeds").getJSONObject(0);
            String videoUrl = data.getString("video_url");

            if (videoUrl != null && !videoUrl.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("msg", "解析成功");
                result.put("data", new JSONObject()
                        .put("author", data.getJSONObject("poster").getString("nick"))
                        .put("avatar", data.getJSONObject("poster").getString("avatar"))
                        .put("time", data.getJSONObject("poster").getString("createtime"))
                        .put("title", data.getString("feed_desc_withat"))
                        .put("cover", data.getJSONArray("images").getJSONObject(0).getString("url"))
                        .put("url", videoUrl));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> weibo(String url) {
        try {
            String id = "";
            if (url.contains("show?fid=")) {
                Pattern pattern = Pattern.compile("fid=(.*)");
                Matcher matcher = pattern.matcher(url);
                if (matcher.find()) {
                    id = matcher.group(1);
                }
            } else {
                Pattern pattern = Pattern.compile("\\d+:\\d+");
                Matcher matcher = pattern.matcher(url);
                if (matcher.find()) {
                    id = matcher.group(0);
                }
            }
            String apiUrl = "YOUR_WEIBO_API_URL" + id; // Replace YOUR_WEIBO_API_URL with the actual API endpoint
            String response = curl(apiUrl);
            JSONObject json = new JSONObject(response);
            JSONObject playInfo = json.getJSONObject("data").getJSONObject("Component_Play_Playinfo");
            Map<String, Object> urls = playInfo.getJSONObject("urls").toMap();
            String videoUrl = (String) urls.get(urls.keySet().iterator().next());
            if (videoUrl != null && !videoUrl.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("msg", "解析成功");
                result.put("data", new JSONObject()
                        .put("author", playInfo.getString("author"))
                        .put("avatar", playInfo.getString("avatar"))
                        .put("time", playInfo.getString("real_date"))
                        .put("title", playInfo.getString("title"))
                        .put("cover", playInfo.getString("cover_image"))
                        .put("url", videoUrl));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }
    public Map<String, Object> kuaishou(String url) {
        try {
            Pattern pattern1 = Pattern.compile("https://v\\.kuaishou\\.com/([\\w\\d]+)");
            Matcher matcher1 = pattern1.matcher(url);

            // 如果找到匹配项，则返回匹配到的视频链接
            if (matcher1.find()) {
                url = "https://v.kuaishou.com/" + matcher1.group(1);
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
                    String title = photo.get("caption") + "";
                    ArrayList mainMvUrls = (ArrayList) photo.get("mainMvUrls");
                    LinkedTreeMap hashMap = (LinkedTreeMap) mainMvUrls.get(0);
                    String videoUrls = hashMap.get("url") + "";
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("title", title);
                    data.put("url", videoUrls);
                    result.put("data", data);
                } else {
                    result.put("code", 400);
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
    }

    public Map<String, Object> lvzhou(String url) {
        try {
            String text = curl(url);

            Pattern titlePattern = Pattern.compile("<div class=\"status-title\">(.*)</div>");
            Matcher titleMatcher = titlePattern.matcher(text);
            String videoTitle = titleMatcher.find() ? titleMatcher.group(1) : "";

            Pattern coverPattern = Pattern.compile("<div style=\"background-image:url\\((.*)\\)");
            Matcher coverMatcher = coverPattern.matcher(text);
            String videoCover = coverMatcher.find() ? coverMatcher.group(1) : "";

            Pattern urlPattern = Pattern.compile("<video src=\"([^\"]*)\"");
            Matcher urlMatcher = urlPattern.matcher(text);
            String videoUrl = urlMatcher.find() ? urlMatcher.group(1) : "";

            Pattern authorPattern = Pattern.compile("<div class=\"nickname\">(.*)</div>");
            Matcher authorMatcher = authorPattern.matcher(text);
            String videoAuthor = authorMatcher.find() ? authorMatcher.group(1) : "";

            Pattern avatarPattern = Pattern.compile("<a class=\"avatar\"><img src=\"(.*)\\?");
            Matcher avatarMatcher = avatarPattern.matcher(text);
            String videoAuthorImg = avatarMatcher.find() ? avatarMatcher.group(1) : "";

            Pattern likePattern = Pattern.compile("已获得(.*)条点赞</div>");
            Matcher likeMatcher = likePattern.matcher(text);
            String videoLike = likeMatcher.find() ? likeMatcher.group(1) : "";

            if (!videoUrl.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("msg", "解析成功");
                result.put("data", new JSONObject()
                        .put("author", videoAuthor)
                        .put("avatar", videoAuthorImg.replace("1080.180", "1080.680"))
                        .put("like", videoLike)
                        .put("title", videoTitle)
                        .put("cover", videoCover)
                        .put("url", videoUrl.replace("amp;", "")));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> zuiyou(String url) {
        try {
            String text = curl(url);
            Pattern videoPattern = Pattern.compile("fullscreen='false' src='(.*?)'");
            Matcher videoMatcher = videoPattern.matcher(text);
            String videoUrl = videoMatcher.find() ? videoMatcher.group(1) : "";
            Pattern titlePattern = Pattern.compile(":<\\/span><h1>(.*?)<\\/h1><\\/div><div class=");
            Matcher titleMatcher = titlePattern.matcher(text);
            String videoTitle = titleMatcher.find() ? titleMatcher.group(1) : "";
            Pattern coverPattern = Pattern.compile("poster='(.*?)'>");
            Matcher coverMatcher = coverPattern.matcher(text);
            String videoCover = coverMatcher.find() ? coverMatcher.group(1) : "";
            videoUrl = videoUrl.replace('\\', '/').replace("u002F", "");
            Pattern authorPattern = Pattern.compile("<span class='SharePostCard__name'>(.*?)<\\/span>");
            Matcher authorMatcher = authorPattern.matcher(text);
            String videoAuthor = authorMatcher.find() ? authorMatcher.group(1) : "";
            if (!videoUrl.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("msg", "解析成功");
                result.put("data", new JSONObject()
                        .put("author", videoAuthor)
                        .put("title", videoTitle)
                        .put("cover", videoCover)
                        .put("url", videoUrl));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> bbq(String url) {
        try {
            String id = "";
            Pattern pattern = Pattern.compile("id=(.*)\\b");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                id = matcher.group(1);
            }
            String apiUrl = "https://bbq.bilibili.com/bbq/app-bbq/sv/detail?svid=" + id;
            String response = curl(apiUrl);
            JSONObject json = new JSONObject(response);
            JSONObject data = json.getJSONObject("data");
            String videoUrl = data.getJSONObject("play")
                    .getJSONArray("file_info")
                    .getJSONObject(0)
                    .getString("url");
            if (videoUrl != null && !videoUrl.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("msg", "解析成功");
                result.put("data", new JSONObject()
                        .put("author", data.getJSONObject("user_info").getString("uname"))
                        .put("avatar", data.getJSONObject("user_info").getString("face"))
                        .put("time", data.getString("pubtime"))
                        .put("like", data.getInt("like"))
                        .put("title", data.getString("title"))
                        .put("cover", data.getString("cover_url"))
                        .put("url", videoUrl));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> quanmin(String id) {
        try {
            if (id.contains("quanmin.baidu.com/v/")) {
                Pattern pattern = Pattern.compile("v/(.*?)\\?");
                Matcher matcher = pattern.matcher(id);
                if (matcher.find()) {
                    id = matcher.group(1);
                }
            }
            String apiUrl = "https://quanmin.hao222.com/wise/growth/api/sv/immerse?source=share-h5&pd=qm_share_mvideo&vid=" + id + "&_format=json";
            String response = curl(apiUrl);
            JSONObject json = new JSONObject(response);
            JSONObject data = json.getJSONObject("data");
            JSONObject author = data.getJSONObject("author");
            JSONObject meta = data.getJSONObject("meta");
            JSONObject videoInfo = meta.getJSONObject("video_info");
            JSONArray clarityUrl = videoInfo.getJSONArray("clarityUrl");
            if (data != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("msg", "解析成功");
                result.put("data", new JSONObject()
                        .put("author", author.getString("name"))
                        .put("avatar", author.getString("icon"))
                        .put("title", meta.getString("title"))
                        .put("cover", meta.getString("image"))
                        .put("url", clarityUrl.getJSONObject(0).getString("url")));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> basai(String id) {
        try {
            String apiUrl = "http://www.moviebase.cn/uread/api/m/video/" + id + "?actionkey=300303";
            String response = curl(apiUrl);
            JSONArray arr = new JSONArray(response);
            JSONObject data = arr.getJSONObject(0).getJSONObject("data");
            String videoUrl = data.getString("videoUrl");
            if (videoUrl != null && !videoUrl.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("msg", "解析成功");
                result.put("data", new JSONObject()
                        .put("time", data.getString("createDate"))
                        .put("title", data.getString("title"))
                        .put("cover", data.getString("coverUrl"))
                        .put("url", videoUrl));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> before(String url) {
        try {
            Pattern pattern = Pattern.compile("detail/(.*)\\?");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                String id = matcher.group(1);
                String apiUrl = "https://hlg.xiatou.com/h5/feed/detail?id=" + id;
                String response = curl(apiUrl);
                JSONObject json = new JSONObject(response);
                JSONArray data = json.getJSONArray("data");
                JSONObject videoInfo = data.getJSONObject(0).getJSONArray("mediaInfoList").getJSONObject(0).getJSONObject("videoInfo");
                String videoUrl = videoInfo.getString("url");
                if (videoUrl != null && !videoUrl.isEmpty()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("code", 200);
                    result.put("msg", "解析成功");
                    result.put("data", new JSONObject()
                            .put("author", data.getJSONObject(0).getJSONObject("author").getString("nickName"))
                            .put("avatar", data.getJSONObject(0).getJSONObject("author").getJSONObject("avatar").getString("url"))
                            .put("like", data.getJSONObject(0).getInt("diggCount"))
                            .put("time", json.getString("recTimeStamp"))
                            .put("title", data.getJSONObject(0).getString("title"))
                            .put("cover", data.getJSONObject(0).getJSONArray("staticCover").getJSONObject(0).getString("url"))
                            .put("url", videoUrl));
                    return result;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> kaiyan(String url) {
        try {
            Pattern pattern = Pattern.compile("\\?vid=(.*)\\b");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                String id = matcher.group(1);
                String apiUrl = "https://baobab.kaiyanapp.com/api/v1/video/" + id + "?f=web";
                String response = curl(apiUrl);
                JSONObject arr = new JSONObject(response);
                String videoApi = "https://baobab.kaiyanapp.com/api/v1/playUrl?vid=" + id +
                        "&resourceType=video&editionType=default&source=aliyun&playUrlType=url_oss&ptl=true";
                String videoUrl = curl(videoApi);
                if (videoUrl != null && !videoUrl.isEmpty()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("code", 200);
                    result.put("msg", "解析成功");
                    result.put("data", new JSONObject()
                            .put("title", arr.getString("title"))
                            .put("cover", arr.getString("coverForFeed"))
                            .put("url", videoUrl));
                    return result;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> momo(String url) {
        try {
            String[] id = new String[1];
            // Use regex to extract the video ID from the URL
            if (url.contains("new-share-v2/")) {
                Pattern pattern = Pattern.compile("new-share-v2/(.*)\\.html");
                Matcher matcher = pattern.matcher(url);
                if (matcher.find()) {
                    id[0] = matcher.group(1);
                }
            } else {
                Pattern pattern = Pattern.compile("momentids=(\\w+)");
                Matcher matcher = pattern.matcher(url);
                if (matcher.find()) {
                    id[0] = matcher.group(1);
                }
            }
            Map<String, String> postData = new HashMap<>();
            postData.put("feedids", id[0]);
            // Make a POST request to retrieve video information
            String apiUrl = "https://m.immomo.com/inc/microvideo/share/profiles";
            String response = curl(apiUrl, postData);
            JSONObject arr = new JSONObject(response);
            JSONObject videoInfo = arr.getJSONObject("data").getJSONArray("list").getJSONObject(0).getJSONObject("video");
            String videoUrl = videoInfo.getString("video_url");
            if (videoUrl != null && !videoUrl.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                JSONObject userInfo = arr.getJSONObject("data").getJSONArray("list").getJSONObject(0).getJSONObject("user");
                result.put("code", 200);
                result.put("msg", "解析成功");
                result.put("data", new JSONObject()
                        .put("author", userInfo.getString("name"))
                        .put("avatar", userInfo.getString("img"))
                        .put("uid", userInfo.getString("momoid"))
                        .put("sex", userInfo.getInt("sex"))
                        .put("age", userInfo.getInt("age"))
                        .put("city", videoInfo.getString("city"))
                        .put("like", videoInfo.getInt("like_cnt"))
                        .put("title", arr.getJSONObject("data").getJSONArray("list").getJSONObject(0).getString("content"))
                        .put("cover", videoInfo.getJSONObject("cover").getString("l"))
                        .put("url", videoUrl));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> vuevlog(String url) {
        try {
            String text = curl(url);
            Pattern titlePattern = Pattern.compile("<title>(.*?)<\\/title>");
            Matcher titleMatcher = titlePattern.matcher(text);
            titleMatcher.find();
            String videoTitle = titleMatcher.group(1);
            Pattern coverPattern = Pattern.compile("<meta name=\"twitter:image\" content=\"(.*?)\">");
            Matcher coverMatcher = coverPattern.matcher(text);
            coverMatcher.find();
            String videoCover = coverMatcher.group(1);
            Pattern urlPattern = Pattern.compile("<meta property=\"og:video:url\" content=\"(.*?)\">");
            Matcher urlMatcher = urlPattern.matcher(text);
            urlMatcher.find();
            String videoUrl = urlMatcher.group(1);
            Pattern authorPattern = Pattern.compile("<div class=\"infoItem name\">(.*?)<\\/div>");
            Matcher authorMatcher = authorPattern.matcher(text);
            authorMatcher.find();
            String videoAuthor = authorMatcher.group(1);
            Pattern avatarPattern = Pattern.compile("<div class=\"avatarContainer\"><img src=\"(.*?)\"");
            Matcher avatarMatcher = avatarPattern.matcher(text);
            avatarMatcher.find();
            String videoAvatar = avatarMatcher.group(1);
            Pattern likePattern = Pattern.compile("<div class=\"likeTitle\">(.*?) friends");
            Matcher likeMatcher = likePattern.matcher(text);
            likeMatcher.find();
            String videoLike = likeMatcher.group(1);
            if (videoUrl != null && !videoUrl.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("msg", "解析成功");
                result.put("data", new JSONObject()
                        .put("author", videoAuthor)
                        .put("avatar", videoAvatar)
                        .put("like", videoLike)
                        .put("title", videoTitle)
                        .put("cover", videoCover)
                        .put("url", videoUrl));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> xiaokaxiu(String url) {
        try {
            Pattern pattern = Pattern.compile("id=(.*)\\b");
            Matcher matcher = pattern.matcher(url);
            matcher.find();
            String id = matcher.group(1);
            String sign = md5("S14OnTD#Qvdv3L=3vm&time=" + System.currentTimeMillis());
            String apiUrl = "https://appapi.xiaokaxiu.com/api/v1/web/share/video/" + id + "?time=" + System.currentTimeMillis();
            Map<String, String> headers = new HashMap<>();
            headers.put("x-sign", sign);
            String response = curl(apiUrl, headers);
            JSONObject arr = new JSONObject(response);
            if (arr.getInt("code") != -2002) {
                Map<String, Object> result = new HashMap<>();
                JSONObject videoInfo = arr.getJSONObject("data").getJSONObject("video").getJSONObject("user");
                result.put("code", 200);
                result.put("msg", "解析成功");
                result.put("data", new JSONObject()
                        .put("author", videoInfo.getString("nickname"))
                        .put("avatar", videoInfo.getString("avatar"))
                        .put("like", arr.getJSONObject("data").getJSONObject("video").getInt("likedCount"))
                        .put("time", arr.getJSONObject("data").getJSONObject("video").getString("createdAt"))
                        .put("title", arr.getJSONObject("data").getJSONObject("video").getString("title"))
                        .put("cover", arr.getJSONObject("data").getJSONObject("video").getString("cover"))
                        .put("url", arr.getJSONObject("data").getJSONObject("video").getJSONArray("url").getString(0)));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> pipigaoxiao(String url) {
        try {
            Pattern pattern = Pattern.compile("post/(.*)");
            Matcher matcher = pattern.matcher(url);
            matcher.find();
            String postId = matcher.group(1);
            String arr1 = curl(postId);
            JSONObject arr = new JSONObject(arr1);
            String id = arr.getJSONObject("data").getJSONObject("post").getJSONArray("imgs").getJSONObject(0).getString("id");
            if (id != null && !id.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("msg", "解析成功");
                result.put("data", new JSONObject()
                        .put("title", arr.getJSONObject("data").getJSONObject("post").getString("content"))
                        .put("cover", "https://file.ippzone.com/img/view/id/" + id)
                        .put("url", arr.getJSONObject("data").getJSONObject("post").getJSONObject("videos").getJSONObject(id).getString("url")));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> quanminkge(String url) {
        try {
            Pattern idPattern = Pattern.compile("\\?s=(.*)");
            Matcher idMatcher = idPattern.matcher(url);
            idMatcher.find();
            String id = idMatcher.group(1);
            String text = curl("https://kg.qq.com/node/play?s=" + id);
            Pattern titlePattern = Pattern.compile("<title>(.*?)-(.*?)-");
            Matcher titleMatcher = titlePattern.matcher(text);
            titleMatcher.find();
            String videoTitle = titleMatcher.group(2);
            Pattern coverPattern = Pattern.compile("cover\":\"(.*?)\"");
            Matcher coverMatcher = coverPattern.matcher(text);
            coverMatcher.find();
            String videoCover = coverMatcher.group(1);
            Pattern urlPattern = Pattern.compile("playurl_video\":\"(.*?)\"");
            Matcher urlMatcher = urlPattern.matcher(text);
            urlMatcher.find();
            String videoUrl = urlMatcher.group(1);
            Pattern avatarPattern = Pattern.compile("{\"activity_id\":0,\"avatar\":\"(.*?)\"");
            Matcher avatarMatcher = avatarPattern.matcher(text);
            avatarMatcher.find();
            String videoAvatar = avatarMatcher.group(1);
            Pattern timePattern = Pattern.compile("<p class=\"singer_more__time\">(.*?)<\\/p>");
            Matcher timeMatcher = timePattern.matcher(text);
            timeMatcher.find();
            String videoTime = timeMatcher.group(1);
            if (videoUrl != null && !videoUrl.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("msg", "解析成功");
                result.put("data", new JSONObject()
                        .put("title", videoTitle)
                        .put("cover", videoCover)
                        .put("url", videoUrl)
                        .put("author", titleMatcher.group(1))
                        .put("avatar", videoAvatar)
                        .put("time", videoTime));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> xigua(String url) {
        try {
            if (url.contains("v.ixigua.com")) {
                URL originalUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) originalUrl.openConnection();
                connection.setInstanceFollowRedirects(false);
                String loc = connection.getHeaderField("Location");
                Pattern idPattern = Pattern.compile("video/(.*)/");
                Matcher idMatcher = idPattern.matcher(loc);
                if (idMatcher.find()) {
                    url = "https://www.ixigua.com/" + idMatcher.group(1);
                }
            }
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");
            headers.put("Cookie", "MONITOR_WEB_ID=7892c49b-296e-4499-8704-e47c1b150c18; ixigua-a-s=1; ttcid=af99669b6304453480454f150701d5c226; BD_REF=1; __ac_nonce=060d88ff000a75e8d17eb; __ac_signature=_02B4Z6wo00f01kX9ZpgAAIDAKIBBQUIPYT5F2WIAAPG2ad; ttwid=1%7CcIsVF_3vqSIk4XErhPB0H2VaTxT0tdsTMRbMjrJOPN8%7C1624806049%7C08ce7dd6f7d20506a41ba0a331ef96a6505d96731e6ad9f6c8c709f53f227ab1");
            String text = curl(url, headers);
            Pattern jsonPattern = Pattern.compile("<script id=\"SSR_HYDRATED_DATA\">window._SSR_HYDRATED_DATA=(.*?)</script>");
            Matcher jsonMatcher = jsonPattern.matcher(text);
            if (jsonMatcher.find()) {
                String jsondata = jsonMatcher.group(1);
                jsondata = jsondata.replace("undefined", "null");
                JSONObject data = new JSONObject(jsondata);
                JSONObject result = data.getJSONObject("anyVideo").getJSONObject("gidInformation").getJSONObject("packerData").getJSONObject("video");
                String videoUrlBase64 = result.getJSONObject("videoResource").getJSONObject("dash").getJSONObject("dynamic_video").getJSONArray("dynamic_video_list").getJSONObject(2).getString("main_url");
                String videoUrl = new String(Base64.getDecoder().decode(videoUrlBase64));
                String musicUrlBase64 = result.getJSONObject("videoResource").getJSONObject("dash").getJSONObject("dynamic_audio").getJSONArray("dynamic_audio_list").getJSONObject(0).getString("main_url");
                String musicUrl = new String(Base64.getDecoder().decode(musicUrlBase64));
                String videoAuthor = result.getJSONObject("user_info").getString("name");
                String videoAvatar = result.getJSONObject("user_info").getString("avatar_url").replace("300x300.image", "300x300.jpg");
                String videoCover = result.getString("poster_url");
                String videoTitle = result.getString("title");
                Map<String, Object> arr = new HashMap<>();
                arr.put("code", 200);
                arr.put("msg", "解析成功");
                arr.put("data", new JSONObject()
                        .put("author", videoAuthor)
                        .put("avatar", videoAvatar)
                        .put("like", result.getInt("video_like_count"))
                        .put("time", result.getString("video_publish_time"))
                        .put("title", videoTitle)
                        .put("cover", videoCover)
                        .put("url", videoUrl)
                        .put("music", new JSONObject().put("url", musicUrl)));
                return arr;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> doupai(String url) {
        try {
            Pattern pattern = Pattern.compile("/topic/(.*?).html");
            Matcher matcher = pattern.matcher(url);
            matcher.find();
            String vid = matcher.group(1);
            String baseUrl = "https://v2.doupai.cc/topic/" + vid + ".json";
            String dataJson = curl(baseUrl);
            JSONObject data = new JSONObject(dataJson);
            String videoUrl = data.getJSONObject("data").getString("videoUrl");
            String title = data.getJSONObject("data").getString("name");
            String cover = data.getJSONObject("data").getString("imageUrl");
            String time = data.getJSONObject("data").getString("createdAt");
            JSONObject author = data.getJSONObject("data").getJSONObject("userId");
            String authorName = author.getString("name");
            String authorAvatar = author.getString("avatar");
            if (videoUrl != null && !videoUrl.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("msg", "解析成功");
                result.put("data", new JSONObject()
                        .put("title", title)
                        .put("cover", cover)
                        .put("time", time)
                        .put("author", authorName)
                        .put("avatar", authorAvatar)
                        .put("url", videoUrl));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> sixroom(String url) {
        try {
            Pattern pattern = Pattern.compile("http[s]?:\\/\\/(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\\(\\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+");
            Matcher matcher = pattern.matcher(url);
            matcher.find();
            String dealUrl = matcher.group(0);
            Map<String, String> headers = new HashMap<>();
            headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.125 Safari/537.36");
            headers.put("x-requested-with", "XMLHttpRequest");
            String rows = curl(dealUrl, headers);
            Pattern tidPattern = Pattern.compile("tid: '(\\w+)',");
            Matcher tidMatcher = tidPattern.matcher(rows);
            tidMatcher.find();
            String tid = tidMatcher.group(1);
            String baseUrl = "https://v.6.cn/message/message_home_get_one.php";
            String content = curl(baseUrl + "?tid=" + tid, headers);
            JSONObject contentJson = new JSONObject(content);
            if (contentJson != null) {
                JSONArray contentArray = contentJson.getJSONArray("content");
                JSONObject data = contentArray.getJSONObject(0).getJSONObject("content");
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("msg", "解析成功");
                result.put("data", new JSONObject()
                        .put("title", data.getString("title"))
                        .put("cover", data.getString("url"))
                        .put("url", data.getString("playurl"))
                        .put("author", contentArray.getJSONObject(0).getString("alias"))
                        .put("avatar", contentArray.getJSONObject(0).getString("userpic")));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> huya(String url) {
        try {
            Pattern vidPattern = Pattern.compile("\\/(\\d+).html");
            Matcher vidMatcher = vidPattern.matcher(url);
            vidMatcher.find();
            String vid = vidMatcher.group(1);
            String api = "https://liveapi.huya.com/moment/getMomentContent";
            Map<String, String> headers = new HashMap<>();
            headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");
            headers.put("referer", "https://v.huya.com/");
            String response = curl(api + "?videoId=" + vid, headers);
            JSONObject content = new JSONObject(response);
            if (content.getInt("status") == 200) {
                String videoUrl = content.getJSONObject("data").getJSONObject("moment").getJSONObject("videoInfo").getJSONArray("definitions").getJSONObject(0).getString("url");
                String cover = content.getJSONObject("data").getJSONObject("moment").getJSONObject("videoInfo").getString("videoCover");
                String title = content.getJSONObject("data").getJSONObject("moment").getJSONObject("videoInfo").getString("videoTitle");
                String avatarUrl = content.getJSONObject("data").getJSONObject("moment").getJSONObject("videoInfo").getString("avatarUrl");
                String author = content.getJSONObject("data").getJSONObject("moment").getJSONObject("videoInfo").getString("nickName");
                String time = content.getJSONObject("data").getJSONObject("moment").getString("cTime");
                int like = content.getJSONObject("data").getJSONObject("moment").getInt("favorCount");
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("msg", "解析成功");
                result.put("data", new JSONObject()
                        .put("title", title)
                        .put("cover", cover)
                        .put("url", videoUrl)
                        .put("time", time)
                        .put("like", like)
                        .put("author", author)
                        .put("avatar", avatarUrl));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> pear(String url) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");
            String html = curl(url, headers);
            Pattern titlePattern = Pattern.compile("<h1 class=\"video-tt\">(.*?)</h1>");
            Matcher titleMatcher = titlePattern.matcher(html);
            titleMatcher.find();
            String title = titleMatcher.group(1);
            Pattern feedIdPattern = Pattern.compile("_(\\d+)");
            Matcher feedIdMatcher = feedIdPattern.matcher(url);
            feedIdMatcher.find();
            String feedId = feedIdMatcher.group(1);
            String baseUrl = String.format("https://www.pearvideo.com/videoStatus.jsp?contId=%s&mrd=%s", feedId, System.currentTimeMillis() / 1000);
            StringBuilder result2 = new StringBuilder();
            URL apiUrl = new URL(baseUrl);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");
            connection.setRequestProperty("Referer", url);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String response;
                while ((response = reader.readLine()) != null) {
                    result2.append(response);
                }
            }
            JSONObject content = new JSONObject(result2.toString());
            if (content.getInt("resultCode") == 1) {
                String video = content.getJSONObject("videoInfo").getJSONObject("videos").getString("srcUrl");
                String cover = content.getJSONObject("videoInfo").getString("video_image");
                long timer = content.getLong("systemTime");
                String videoUrl = video.replace(String.valueOf(timer), "cont-" + feedId);
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("msg", "解析成功");
                result.put("data", new JSONObject()
                        .put("title", title)
                        .put("cover", cover)
                        .put("url", videoUrl)
                        .put("time", timer));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> xinpianchang(String url) {
        try {
            Map<String, String> homeHeaders = new HashMap<>();
            homeHeaders.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.125 Safari/537.36");
            homeHeaders.put("upgrade-insecure-requests", "1");
            String html = curl(url, homeHeaders);
            Pattern keyPattern = Pattern.compile("var modeServerAppKey = \"(.*?)\";");
            Matcher keyMatcher = keyPattern.matcher(html);
            keyMatcher.find();
            String key = keyMatcher.group(1);
            Pattern vidPattern = Pattern.compile("var vid = \"(.*?)\";");
            Matcher vidMatcher = vidPattern.matcher(html);
            vidMatcher.find();
            String vid = vidMatcher.group(1);
            String baseUrl = String.format("https://mod-api.xinpianchang.com/mod/api/v2/media/%s?appKey=%s&extend=%s", vid, key, "userInfo,userStatus");
            Map<String, String> apiHeaders = new HashMap<>();
            apiHeaders.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.125 Safari/537.36");
            apiHeaders.put("referer", url);
            apiHeaders.put("origin", "https://www.xinpianchang.com");
            apiHeaders.put("content-type", "application/json");
            StringBuilder result2 = new StringBuilder();
            URL apiUrl = new URL(baseUrl);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            for (Map.Entry<String, String> entry : apiHeaders.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            connection.setRequestProperty("referer", url);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result2.append(line);
                }
            }
            String response = result2.toString();
            JSONObject content = new JSONObject(response);
            if (content.getInt("status") == 0) {
                String cover = content.getJSONObject("data").getString("cover");
                String title = content.getJSONObject("data").getString("title");
                String author = content.getJSONObject("data").getJSONObject("owner").getString("username");
                String avatar = content.getJSONObject("data").getJSONObject("owner").getString("avatar");
                JSONArray videos = content.getJSONObject("data").getJSONObject("resource").getJSONArray("progressive");
                List<JSONObject> videoList = new ArrayList<>();
                for (int i = 0; i < videos.length(); i++) {
                    JSONObject videoObj = videos.getJSONObject(i);
                    JSONObject videoItem = new JSONObject();
                    videoItem.put("profile", videoObj.getString("profile"));
                    videoItem.put("url", videoObj.getString("url"));
                    videoList.add(videoItem);
                }
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("msg", "解析成功");
                result.put("data", new JSONObject()
                        .put("author", author)
                        .put("avatar", avatar)
                        .put("cover", cover)
                        .put("title", title)
                        .put("url", videoList));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> acfan(String url) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");
            String html = curl(url, headers);
            Pattern infoPattern = Pattern.compile("var videoInfo =\\s(.*?);");
            Matcher infoMatcher = infoPattern.matcher(html);
            infoMatcher.find();
            JSONObject videoInfo = new JSONObject(trim(infoMatcher.group(1)));
            Pattern playPattern = Pattern.compile("var playInfo =\\s(.*?);");
            Matcher playMatcher = playPattern.matcher(html);
            playMatcher.find();
            JSONObject playInfo = new JSONObject(trim(playMatcher.group(1)));
            if (html != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("msg", "解析成功");
                result.put("data", new JSONObject()
                        .put("title", videoInfo.getString("title"))
                        .put("cover", videoInfo.getString("cover"))
                        .put("url", playInfo.getJSONArray("streams").getJSONObject(0).getJSONArray("playUrls").getString(0)));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
    }

    public Map<String, Object> meipai(String url) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");
            String html = curl(url, headers);
            Pattern contentPattern = Pattern.compile("\'data-video='(.*?)'\'");
            Matcher contentMatcher = contentPattern.matcher(html);
            contentMatcher.find();
            String video_bs64 = contentMatcher.group(1);

            Pattern titlePattern = Pattern.compile("<meta name='description' content='(.*?)'");
            Matcher titleMatcher = titlePattern.matcher(html);
            titleMatcher.find();
            String title = titleMatcher.group(1);
            if (video_bs64 != null) {
                String video = new String(Base64.getDecoder().decode(video_bs64));
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("msg", "解析成功");
                result.put("data", new JSONObject()
                        .put("title", title)
                        .put("url", video));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 201);
            errorResponse.put("msg", "解析失败");
            return errorResponse;
        }
        return null;
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
}
