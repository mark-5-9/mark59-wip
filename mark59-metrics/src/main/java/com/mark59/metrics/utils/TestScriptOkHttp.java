package com.mark59.metrics.utils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import com.mark59.metrics.pojos.ParsedMetric;
import com.mark59.metrics.pojos.ScriptResponse;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class TestScriptOkHttp {


	public static void main(String[] args) {

		String newRelicApiAppId  	= "newRelicApiAppId";
		String newRelicXapiKey  	= "newRelicXapiKey";  //harvest
		String proxyServer			=  ""; //"proxyServer"
		String proxyPort			=  ""; //"proxyPort"

		String newRelicApiUrl = "https://api.newrelic.com/v2/applications/";
		String url = newRelicApiUrl + newRelicApiAppId + "/hosts.json";
		ScriptResponse scriptResponse = new ScriptResponse();
		List<ParsedMetric> parsedMetrics = new ArrayList<ParsedMetric>();

		Request request; Response response = null; JSONObject jsonResponse = null;
		Proxy proxy = StringUtils.isNotBlank(proxyServer + proxyPort) ? new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyServer , new Integer(proxyPort))) : null;
		OkHttpClient client = proxy != null ? new OkHttpClient.Builder().proxy(proxy).build() : new OkHttpClient();
		Headers headers = new Headers.Builder().add("X-Api-Key", newRelicXapiKey).add("Content-Type", "application/json").build();
//		String debugJsonResponses =  "running profile " + serverProfile.serverProfileName + ", init req : " + url ;
		String debugJsonResponses =  "running profile " + "GREGS HARVERST PROFILE" + ", init req : " + url ;

		try {

			request = new Request.Builder().url(url).headers(headers).get().   build();
			response = client.newCall(request).execute();
			jsonResponse = new JSONObject(response.body().string());
			debugJsonResponses =  debugJsonResponses + "<br>init res.: " + jsonResponse.toString();

			ZonedDateTime utcTimeNow = ZonedDateTime.now(ZoneOffset.UTC);
			String toHour 	= String.format("%02d", utcTimeNow.getHour());
			String toMinute	= String.format("%02d", utcTimeNow.getMinute());
			ZonedDateTime utcMinus1Min = utcTimeNow.minusMinutes(1);
			String fromHour	= String.format("%02d", utcMinus1Min.getHour());
			String fromMinute = String.format("%02d", utcMinus1Min.getMinute());
			String fromDate = utcMinus1Min.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			String toDate 	= utcTimeNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			String urlDateRangeParmStr = "&from=" + fromDate + "T" + fromHour + "%3A" + fromMinute + "%3A00%2B00%3A00" + "&to=" + toDate + "T" + toHour + "%3A" + toMinute + "%3A00%2B00%3A00";

			JSONArray application_hosts = jsonResponse.getJSONArray("application_hosts");

			for (int i = 0; i < application_hosts.length(); i++) {
				JSONObject application_host = (JSONObject) application_hosts.get(i);
				Integer hostId = (Integer) application_host.get("id");
				String hostName = ((String)application_host.get("host")).replace(":","_");
//				url = newRelicApiUrl + newRelicApiAppId  + "/hosts/" + hostId + "/metrics/data.json?names%5B%5D=Memory%2FHeap%2FFree&names%5B%5D=CPU%2FUser%2FUtilization" + urlDateRangeParmStr;
				url = newRelicApiUrl + newRelicApiAppId  + "/hosts/" + hostId + "/metrics/data.json?names%5B%5D=Memory%2FHeap%2FFree&names%5B%5D=CPU/User Time" + urlDateRangeParmStr;
				debugJsonResponses =  debugJsonResponses + "<br><br>req." + i + ": " + url ;

				request = new Request.Builder().url(url).headers(headers).get().build();
				response = client.newCall(request).execute();
				jsonResponse = new JSONObject(response.body().string());
				debugJsonResponses =  debugJsonResponses + "<br>res." + i + ": " + jsonResponse.toString();

				Number totalUusedMbMemory = -1.0;
				totalUusedMbMemory =  (Number)((JSONObject)((JSONObject)jsonResponse.getJSONObject("metric_data").getJSONArray("metrics").get(0)).getJSONArray("timeslices").get(0)).getJSONObject("values").get("total_used_mb") ;
				parsedMetrics.add(new ParsedMetric("MEMORY_" + hostName, totalUusedMbMemory,  "MEMORY"));

				Number percentCpuUserUtilization = -1.0;
				percentCpuUserUtilization = (Number)((JSONObject)((JSONObject)jsonResponse.getJSONObject("metric_data").getJSONArray("metrics").get(1)).getJSONArray("timeslices").get(0)).getJSONObject("values").get("percent");
				parsedMetrics.add(new ParsedMetric("CPU_" + hostName, percentCpuUserUtilization, "CPU_UTIL"));

			}
		} catch (Exception e) {
			debugJsonResponses =  debugJsonResponses + "<br>\n ERROR :  Exception last url: " + url + ", response of  : " + jsonResponse + ", message: "+ e.getMessage();
		}
		scriptResponse.setCommandLog(debugJsonResponses);
		scriptResponse.setParsedMetrics(parsedMetrics);
		System.out.println(scriptResponse.getCommandLog());
		System.out.println(scriptResponse.getParsedMetrics());
//		return scriptResponse;

	}


}
