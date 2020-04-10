/*
 * Copyright (C) 2002 by Michael Pitoniak (pitoniakm@msn.com)
 * All rights are reserved.
 * Reproduction and/or redistribution in whole or in part is expressly
 * prohibited without the written consent of the copyright owner.
 *
 * This Software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

package utils.testrail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import apache.commons.config.ApacheCommonsConfigServices;
import utils.file.FileServices;
import utils.gson.GsonServices;
import utils.log4j.Log4jServices;
import utils.log4j.Log4jServicesConstants;
import utils.logger.LoggerServices;




public class TestRailServices {
	private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
	private String serverUrl;
	private String userName;
	private String passWord;
	private Integer retryCnt = TestRailConstants.RETRY_DEFAULT;
	private Integer retrySleepInterval = TestRailConstants.RETRY_SLEEP_DEFAULT;
	
	
	
	private Configuration config = null;
	private CloseableHttpClient httpClient = null;
	private RequestConfig requestConfig = null;

	
	public static Builder builder(String serverUrl){
        return new Builder(serverUrl);
    }
	
	
	public static class Builder {
		private String serverUrl;
		private String userName;
		private String passWord;
		private Integer retryCnt;
		private Integer retrySleepInterval;

		
		private Builder(String serverUrl) {
			this.serverUrl = serverUrl;
		}

		public Builder userName(String userName) {
			this.userName = userName;
			return this;
		}

		public Builder passWord(String passWord) {
			this.passWord = passWord;
			return this;
		}

		public Builder retryCnt(Integer retryCnt) {
			this.retryCnt = retryCnt;
			return this;
		}

		public Builder retrySleepInterval(Integer retrySleepInterval) {
			this.retrySleepInterval = retrySleepInterval;
			return this;
		}

		public TestRailServices build() {
			return new TestRailServices(this);
		}
	}

	private TestRailServices(Builder builder) {
		this.serverUrl = builder.serverUrl;
		this.userName = builder.userName;
		this.passWord = builder.passWord;
		this.retryCnt = builder.retryCnt;
		this.retrySleepInterval = builder.retrySleepInterval;

		CredentialsProvider provider = new BasicCredentialsProvider();
		provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, passWord));

		httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
		//TODO bring in from builder
		requestConfig = RequestConfig.custom().setConnectTimeout(30000).setConnectionRequestTimeout(30000)
				.setSocketTimeout(30000).build();
	}

	public TestRailServices(String propertyFilePath) throws KeyManagementException, NoSuchAlgorithmException,
			KeyStoreException, IOException, ConfigurationException {
		LogManager.getRootLogger().setLevel(Level.DEBUG);
		if (propertyFilePath == null) {
			propertyFilePath = FileServices.build().findFileInTree("testrail.properties");
		}
		if (propertyFilePath != null) {
			LOGGER.info(LoggerServices.build().bannerWrap("TestRail Properties File",
					propertyFilePath + "\n" + FileServices.build().getFileContents(propertyFilePath)));
			config = ApacheCommonsConfigServices.build().load(propertyFilePath);
		} else {
			LOGGER.info(LoggerServices.build().bannerWrap("using testrail.properties in JAR"));
			config = ApacheCommonsConfigServices.build().loadUrl("testrail.properties");
		}

		LOGGER.info("{}", config.getString("url"));
		LOGGER.info("{}", config.getString("user"));
		LOGGER.info("{}", config.getString("password"));

		retryCnt = Integer.valueOf(config.getString("retryCnt"));
		retrySleepInterval = Integer.valueOf(config.getString("retrySleepInterval"));


		this.serverUrl = config.getString("url");
		this.userName = config.getString("user");
		this.passWord = config.getString("password");
		CredentialsProvider provider = new BasicCredentialsProvider();
		provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, passWord));

		httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
		//TODO bring in from properties file
		requestConfig = RequestConfig.custom().setConnectTimeout(30000).setConnectionRequestTimeout(30000)
				.setSocketTimeout(30000).build();
	}

	//TODO remove
	public JsonObject updateTestCase(String projectName, String mileStoneName, String suiteName, String sectionName,
			TYPE type, CATEGORY category, String testCaseName, PRIORITY priority, String focus, String estimate,
			String preconds, String steps, String expected, String assignedToName)
			throws IOException, TestRailConfigException {
		HashMap<String, Object> config = new HashMap<>();

		Integer projectId = getProjectIdByName(projectName, true);
		Integer mileStoneId = getMileStoneIdByName(projectId, mileStoneName, true);
		Integer suiteId = getSuiteIdByName(projectId, suiteName, true);
		Integer sectionId = getSectionIdByName(projectId, suiteId, sectionName, true);
		Integer testCaseId = getTestCaseIdByName(projectId, suiteId, sectionId, testCaseName, true);
		JsonBuilderFactory factory = Json.createBuilderFactory(config);

		//TODO complete
		javax.json.JsonObject json = factory.createObjectBuilder().add("title", testCaseName)
				.add("custom_focus", (focus == null || focus.equals("")) ? "N/A" : focus)
				.add("priority_id", priority.priority()).add("custom_preconds", preconds).add("custom_steps", steps)
				.add("customs_expected", expected).add("customs_expected", expected)
				//TODO verify fix
				.add("focus", focus).add("type_id", getTestCaseTypeIdByName(type.type(), true))
				.add("custom_exectype", category.getValue()).add("milestone_id", mileStoneId).add("estimate", estimate)
				.add("assignedto_id", getUserIdByName(assignedToName, true)).build();

		return updateTestCase(testCaseId, json.toString());

	}



	public JsonObject postTestResults(String projectName, String mileStoneName, String runName, String suiteName,
			String sectionName, String testCaseName, String description, String assignedToName, String version,
			String elapsed, String defects, TEST_STATUS status, boolean failOnError)
			throws IOException, TestRailConfigException {
		JsonObject jsonObject = null;
		try {
			LOGGER.debug(LoggerServices.build().bannerWrap("Posting Results",
					"Project: " + projectName + "\n" + "MileStone: " + mileStoneName + "\n" + "Run: " + runName + "\n"
							+ "Suite: " + suiteName + "\n" + "Section: " + sectionName + "\n" + "TestCase: "
							+ testCaseName + "\n" + "Description: " + description + "\n" + "Assigned to: "
							+ assignedToName + "\n" + "version: " + version + "\n" + "Status: " + status));
			Integer runId = getRunIdByName(projectName, runName, true);
			//LOGGER.debug("runId: {}", runId);
			Integer testCaseId = getTestCaseBuilder(testCaseName).projectName(projectName).suiteName(suiteName)
					.sectionName(sectionName).build(true).getTestCaseId();

			// Integer testCaseId = getTestCaseIdByName(projectName, suiteName, sectionName, testCaseName, true);

			/*Map<String, Object> data = new HashMap<String, Object>();
			
			data.put("status_id", status.getValue());
			if(description!=null){
			    data.put("comment", description);
			}
			if(version!=null){
			    data.put("version", version);
			}
			if(elapsed!=null){
			    data.put("elapsed", elapsed);
			}
			if(defects!=null){
			    data.put("defects", defects);
			}
			if(!assignedToName.equals(TestRailConstants.ASSIGNED_TO_DEFAULT)){
			    Integer userId = getUserIdByName(assignedToName, true);
			    if(userId!=null){
			        data.put("assignedto_id", userId);
			    }else{
			        LOGGER.error("Assigned To name not found: " + assignedToName);
			    }
			}*/
			HashMap<String, Object> config = new HashMap<>();
			JsonBuilderFactory factory = Json.createBuilderFactory(config);
			JsonObjectBuilder jsonObjectBuilder = factory.createObjectBuilder().add("status_id", status.getValue());

			if (description != null) {
				jsonObjectBuilder.add("comment", description);
			}
			if (version != null) {
				jsonObjectBuilder.add("version", version);
			}
			if (elapsed != null) {
				jsonObjectBuilder.add("elapsed", elapsed);
			}
			if (defects != null) {
				jsonObjectBuilder.add("defects", defects);
			}
			if (!assignedToName.equals(TestRailConstants.ASSIGNED_TO_DEFAULT)) {
				Integer userId = getUserIdByName(assignedToName, true);
				if (userId != null) {
					jsonObjectBuilder.add("assignedto_id", userId);
				} else {
					LOGGER.error("Assigned To name not found: " + assignedToName);
				}
			}
			javax.json.JsonObject json = jsonObjectBuilder.build();

			//jsonObject= addRun(projectId, json.toString());

			jsonObject = postTestResults(runId, testCaseId, json.toString());
		} catch (Exception e) {
			if (failOnError) {
				throw e;
			} else {
				FileServices.build().write("errors.txt", e.getMessage(), true);
				LOGGER.error("{}\n{}", e.getMessage() == null ? "" : e.getMessage(), ExceptionUtils.getStackTrace(e));
			}
		}

		return jsonObject;
	}


	/*
	 * 
	 *  Core Methods
	 * 
	 * 
	 */

	public JsonArray getProjects() throws IOException {
		CloseableHttpResponse response = null;
		JsonArray jsonArray = null;
		int retry = retryCnt;
		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("get_projects").build();
				LOGGER.debug(LoggerServices.build().bannerWrap("Rest URI", request.toString(), '#'));

				HttpGet httpGet = new HttpGet(request);
				httpGet.setConfig(requestConfig);
				httpGet.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				// Get HttpResponse Status
				LOGGER.debug("{}", response.getProtocolVersion()); // HTTP/1.1
				LOGGER.debug("{}", response.getStatusLine().getStatusCode()); // 200
				LOGGER.debug("{}", response.getStatusLine().getReasonPhrase()); // OK
				LOGGER.debug("{}", response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonArray = (JsonArray) new JsonParser().parse(EntityUtils.toString(httpEntity));
			} catch (Exception e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}
		return jsonArray;
	}

	public JsonObject getProject(Integer projectId, boolean bFailOnNotFound) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;
		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("get_project/" + projectId).build();

				LOGGER.debug(LoggerServices.build().bannerWrap("Rest URI", request.toString(), '#'));

				HttpGet httpGet = new HttpGet(request);
				httpGet.setConfig(requestConfig);
				httpGet.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				// Get HttpResponse Status
				LOGGER.debug("{}", response.getProtocolVersion()); // HTTP/1.1
				LOGGER.debug("{}", response.getStatusLine().getStatusCode()); // 200
				LOGGER.debug("{}", response.getStatusLine().getReasonPhrase()); // OK
				LOGGER.debug("{}", response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				//TODO make sure consistant pattern
				if (bFailOnNotFound && jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}
				jsonObject = (JsonObject) new JsonParser().parse(EntityUtils.toString(httpEntity));
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	public JsonArray getProjects(String projectName) throws IOException {
		JsonArray jsonArray = new JsonArray();
		JsonArray projectArray = getProjects();

		for (JsonElement jsonElement : projectArray) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			LOGGER.trace("comparing: {} to: {}", jsonObject.get("name"), projectName);
			if (jsonObject.get("name").getAsString().equals(projectName)) {
				LOGGER.debug("adding project: {}", jsonObject.get("name"));
				jsonArray.add(jsonObject);
			}
		}
		return jsonArray;
	}

	public JsonObject getProjectByName(String projectName, boolean bFailOnNotFound)
			throws TestRailConfigException, IOException {
		JsonArray projectArray = getProjects(projectName);

		if (bFailOnNotFound && projectArray.size() == 0) {
			throw new IOException("Project not found: " + projectName + "\n" + projectArray);
		}
		if (projectArray.size() > 1) {
			throw new TestRailConfigException("Project name not unique: " + projectName);
		}

		return projectArray.size() == 0 ? null : (JsonObject) projectArray.get(0);
	}

	public Integer getProjectIdByName(String projectName, boolean bFailOnNotFound)
			throws TestRailConfigException, IOException {
		JsonObject jsonObject = getProjectByName(projectName, bFailOnNotFound);
		return jsonObject == null ? null : jsonObject.get("id").getAsInt();
	}

	public JsonObject deleteProject(Integer projectId, boolean bFailOnNotFound) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;

		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("delete_project" + projectId).build();
				HttpPost httpPost = new HttpPost(request);
				httpPost.setConfig(requestConfig);
				httpPost.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == 400 && bFailOnNotFound) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				} else if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonObject = jsonElement.getAsJsonObject();

				break;
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	public Boolean isProjectExists(Integer projectId) throws IOException, TestRailConfigException {
		boolean exists = false;
		JsonObject jsonObject = getProject(projectId, false);
		exists = jsonObject != null;

		return exists;
	}

	public JsonObject addProject(String json) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;

		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("add_project").build();
				HttpPost httpPost = new HttpPost(request);
				httpPost.setConfig(requestConfig);
				httpPost.addHeader("Content-Type", "application/json");
				httpPost.setEntity(new StringEntity(json));
				response = httpClient.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonObject = jsonElement.getAsJsonObject();

				break;
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	public JsonObject addPlan(Integer projectId, String json) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;

		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("add_plan/" + projectId).build();
				HttpPost httpPost = new HttpPost(request);
				httpPost.setConfig(requestConfig);
				httpPost.addHeader("Content-Type", "application/json");
				httpPost.setEntity(new StringEntity(json));
				response = httpClient.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}
				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				break;
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	public JsonObject addRun(Integer projectId, String json) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;

		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("add_run/" + projectId).build();
				HttpPost httpPost = new HttpPost(request);
				httpPost.setConfig(requestConfig);
				httpPost.addHeader("Content-Type", "application/json");
				httpPost.setEntity(new StringEntity(json));
				response = httpClient.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}
				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				break;
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	public JsonObject addMileStone(Integer projectId, String json) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;

		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("add_milestone/" + projectId).build();
				HttpPost httpPost = new HttpPost(request);
				httpPost.setConfig(requestConfig);
				httpPost.addHeader("Content-Type", "application/json");
				httpPost.setEntity(new StringEntity(json));
				response = httpClient.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}
				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				break;
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	public JsonObject addSuite(Integer projectId, String json) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;

		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("add_suite/" + projectId).build();
				HttpPost httpPost = new HttpPost(request);
				httpPost.setConfig(requestConfig);
				httpPost.addHeader("Content-Type", "application/json");
				httpPost.setEntity(new StringEntity(json));
				response = httpClient.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}
				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				break;
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	public JsonObject addSection(Integer projectId, String json) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;

		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("add_section/" + projectId).build();
				HttpPost httpPost = new HttpPost(request);
				httpPost.setConfig(requestConfig);
				httpPost.addHeader("Content-Type", "application/json");
				httpPost.setEntity(new StringEntity(json));
				response = httpClient.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}
				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				break;
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	/*
	    title  string  The title of the test case (required)
	     template_id int The ID of the template (field layout) (requires TestRail 5.2 or later)
	     type_id int The ID of the case type
	     priority_id int The ID of the case priority
	     estimate    timespan    The estimate, e.g. "30s" or "1m 45s"
	     milestone_id    int The ID of the milestone to link to the test case
	     refs    string  A comma-separated list of references/requirements
	*/
	public JsonObject addTestCase(Integer sectionId, String json) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;

		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("add_case/" + sectionId).build();
				HttpPost httpPost = new HttpPost(request);
				httpPost.setConfig(requestConfig);
				httpPost.addHeader("Content-Type", "application/json");
				httpPost.setEntity(new StringEntity(json));
				response = httpClient.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}
				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				break;
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	public JsonObject addResultForCase(Integer runId, Integer testCaseId, String json) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;

		LOGGER.info(LoggerServices.build().bannerWrap("addResultForCase: " + json));
		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("add_result_for_case/" + runId + "/" + testCaseId)
						.build();
				HttpPost httpPost = new HttpPost(request);
				httpPost.setConfig(requestConfig);
				httpPost.addHeader("Content-Type", "application/json");
				httpPost.setEntity(new StringEntity(json));
				response = httpClient.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}
				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				break;
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	/*
	 	This method expects an array of test results (via the 'results' field, please see below). 
	 	Each test result must specify the test case ID and can pass in the same fields as 
	 	add_result, namely all test related system and custom fields.
	 	
	 	{
			"results": [
				{
					"case_id": 1,
					"status_id": 5,
					"comment": "This test failed",
					"defects": "TR-7"
		
				},
				{
					"case_id": 2,
					"status_id": 1,
					"comment": "This test passed",
					"elapsed": "5m",
					"version": "1.0 RC1"
				}
			}
	
	 * 
	 */
	/*
		Adds one or more new test results, comments or assigns one or more tests. 
		Ideal for test automation to bulk-add multiple test results in one step.
		This method expects an array of test results (via the 'results' field, please see below). 
		Each test result must specify the test ID and can pass in the same fields as add_result, namely 
		all test related system and custom fields.
	
		Please note that all referenced tests must belong to the same test run.
	 * 
	 */
	public JsonObject addResults(Integer runId, String json) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;

		LOGGER.debug(LoggerServices.build().bannerWrap("POSTING RESULTS  TO TESTRAIL",
				GsonServices.build().prettyPrint(json), '#'));
		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("add_results/" + runId).build();
				HttpPost httpPost = new HttpPost(request);
				httpPost.setConfig(requestConfig);
				httpPost.addHeader("Content-Type", "application/json");
				httpPost.setEntity(new StringEntity(json));
				response = httpClient.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}
				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				break;
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	public JsonObject deleteMileStone(Integer mileStoneId, boolean bFailOnNotFound) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;

		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("delete_milestone/" + mileStoneId).build();
				HttpPost httpPost = new HttpPost(request);
				httpPost.setConfig(requestConfig);
				httpPost.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == 400 && bFailOnNotFound) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				} else if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}
				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				break;
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	public JsonObject deletePlan(Integer planId, boolean bFailOnNotFound) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;

		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("delete_plan" + planId).build();
				HttpPost httpPost = new HttpPost(request);
				httpPost.setConfig(requestConfig);
				httpPost.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == 400 && bFailOnNotFound) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				} else if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}
				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				break;
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	public JsonObject deleteRun(Integer runId, boolean bFailOnNotFound) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;

		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("delete_run/" + runId).build();
				HttpPost httpPost = new HttpPost(request);
				httpPost.setConfig(requestConfig);
				httpPost.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == 400 && bFailOnNotFound) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				} else if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}
				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				break;
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	public JsonObject deleteSection(Integer sectionId, boolean bFailOnNotFound) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;

		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("delete_section/" + sectionId).build();
				HttpPost httpPost = new HttpPost(request);
				httpPost.setConfig(requestConfig);
				httpPost.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == 400 && bFailOnNotFound) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				} else if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}
				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				break;
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	public JsonObject deleteSuite(Integer suiteId, boolean bFailOnNotFound) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;

		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("delete_suite/" + suiteId).build();
				HttpPost httpPost = new HttpPost(request);
				httpPost.setConfig(requestConfig);
				httpPost.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == 400 && bFailOnNotFound) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				} else if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}
				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				break;
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	/*200	Success, the project was deleted
	400	Invalid or unknown project
	403	No permissions to delete projects (requires admin rights)*/
	public JsonObject deleteTestCase(Integer testCaseId, boolean bFailOnNotFound) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;

		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("delete_case/" + testCaseId).build();
				HttpPost httpPost = new HttpPost(request);
				httpPost.setConfig(requestConfig);
				httpPost.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == 400 && bFailOnNotFound) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				} else if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}
				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				break;
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	//TODO do for all
	//TODO check for multiple returns in methods
	public JsonObject getTestCase(Integer testCaseId, boolean bFailOnNotFound) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;
		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("get_case/" + testCaseId).build();

				LOGGER.debug(LoggerServices.build().bannerWrap("Rest URI", request.toString(), '#'));

				HttpGet httpGet = new HttpGet(request);
				httpGet.setConfig(requestConfig);
				httpGet.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				// Get HttpResponse Status
				LOGGER.debug("{}", response.getProtocolVersion()); // HTTP/1.1
				LOGGER.debug("{}", response.getStatusLine().getStatusCode()); // 200
				LOGGER.debug("{}", response.getStatusLine().getReasonPhrase()); // OK
				LOGGER.debug("{}", response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (bFailOnNotFound && jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonObject = (JsonObject) new JsonParser().parse(EntityUtils.toString(httpEntity));
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	public JsonObject getSuite(Integer suiteId, boolean bFailOnNotFound) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;
		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("get_suite/" + suiteId).build();

				LOGGER.debug(LoggerServices.build().bannerWrap("Rest URI", request.toString(), '#'));

				HttpGet httpGet = new HttpGet(request);
				httpGet.setConfig(requestConfig);
				httpGet.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				// Get HttpResponse Status
				LOGGER.debug("{}", response.getProtocolVersion()); // HTTP/1.1
				LOGGER.debug("{}", response.getStatusLine().getStatusCode()); // 200
				LOGGER.debug("{}", response.getStatusLine().getReasonPhrase()); // OK
				LOGGER.debug("{}", response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (bFailOnNotFound && jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonObject = (JsonObject) new JsonParser().parse(EntityUtils.toString(httpEntity));
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	public JsonArray getRuns(Integer projectId) throws IOException {
		CloseableHttpResponse response = null;
		JsonArray jsonArray = null;
		int retry = retryCnt;
		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("get_runs/" + projectId).build();
				LOGGER.debug(LoggerServices.build().bannerWrap("Rest URI", request.toString(), '#'));

				HttpGet httpGet = new HttpGet(request);
				httpGet.setConfig(requestConfig);
				httpGet.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				// Get HttpResponse Status
				LOGGER.debug("{}", response.getProtocolVersion()); // HTTP/1.1
				LOGGER.debug("{}", response.getStatusLine().getStatusCode()); // 200
				LOGGER.debug("{}", response.getStatusLine().getReasonPhrase()); // OK
				LOGGER.debug("{}", response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonArray = (JsonArray) new JsonParser().parse(EntityUtils.toString(httpEntity));
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonArray;
	}

	public JsonArray getMileStones(Integer projectId) throws IOException {
		CloseableHttpResponse response = null;
		JsonArray jsonArray = null;
		int retry = retryCnt;
		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("get_milestones/" + projectId).build();
				LOGGER.debug(LoggerServices.build().bannerWrap("Rest URI", request.toString(), '#'));

				HttpGet httpGet = new HttpGet(request);
				httpGet.setConfig(requestConfig);
				httpGet.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				// Get HttpResponse Status
				LOGGER.debug("{}", response.getProtocolVersion()); // HTTP/1.1
				LOGGER.debug("{}", response.getStatusLine().getStatusCode()); // 200
				LOGGER.debug("{}", response.getStatusLine().getReasonPhrase()); // OK
				LOGGER.debug("{}", response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonArray = (JsonArray) new JsonParser().parse(EntityUtils.toString(httpEntity));
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}
		return jsonArray;
	}

	public JsonArray getSuites(Integer suiteId) throws IOException {
		CloseableHttpResponse response = null;
		JsonArray jsonArray = null;
		int retry = retryCnt;
		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("get_suites/" + suiteId).build();
				LOGGER.debug(LoggerServices.build().bannerWrap("Rest URI", request.toString(), '#'));

				HttpGet httpGet = new HttpGet(request);
				httpGet.setConfig(requestConfig);
				httpGet.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				// Get HttpResponse Status
				LOGGER.debug("{}", response.getProtocolVersion()); // HTTP/1.1
				LOGGER.debug("{}", response.getStatusLine().getStatusCode()); // 200
				LOGGER.debug("{}", response.getStatusLine().getReasonPhrase()); // OK
				LOGGER.debug("{}", response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonArray = (JsonArray) new JsonParser().parse(EntityUtils.toString(httpEntity));
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonArray;
	}

	public JsonArray getSections(Integer projectId, Integer suiteId) throws IOException {
		CloseableHttpResponse response = null;
		JsonArray jsonArray = null;
		int retry = retryCnt;
		while (retry >= 0) {
			try {
				UriBuilder uriBuilder = UriBuilder.fromPath(serverUrl).path("get_sections/" + projectId);

				if (suiteId != null) {
					uriBuilder.queryParam("suite_id", suiteId);
				}
				URI request = uriBuilder.build();
				LOGGER.info("{}", request);
				LOGGER.debug(LoggerServices.build().bannerWrap("Rest URI", request.toString(), '#'));

				HttpGet httpGet = new HttpGet(request);
				httpGet.setConfig(requestConfig);
				httpGet.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				// Get HttpResponse Status
				LOGGER.debug("{}", response.getProtocolVersion()); // HTTP/1.1
				LOGGER.debug("{}", response.getStatusLine().getStatusCode()); // 200
				LOGGER.debug("{}", response.getStatusLine().getReasonPhrase()); // OK
				LOGGER.debug("{}", response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonArray = (JsonArray) new JsonParser().parse(EntityUtils.toString(httpEntity));
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonArray;
	}

	public JsonArray getTestCases(Integer projectId, Integer suiteId, Integer sectionId) throws IOException {
		CloseableHttpResponse response = null;
		JsonArray jsonArray = null;
		int retry = retryCnt;
		while (retry >= 0) {
			try {
				UriBuilder uriBuilder = UriBuilder.fromPath(serverUrl).path("get_cases/" + projectId);

				if (suiteId != null) {
					uriBuilder.queryParam("suite_id", suiteId);
				}
				if (sectionId != null) {
					uriBuilder.queryParam("section_id", sectionId);
				}
				URI request = uriBuilder.build();
				LOGGER.info("{}", request);
				LOGGER.debug(LoggerServices.build().bannerWrap("Rest URI", request.toString(), '#'));

				HttpGet httpGet = new HttpGet(request);
				httpGet.setConfig(requestConfig);
				httpGet.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				// Get HttpResponse Status
				LOGGER.debug("{}", response.getProtocolVersion()); // HTTP/1.1
				LOGGER.debug("{}", response.getStatusLine().getStatusCode()); // 200
				LOGGER.debug("{}", response.getStatusLine().getReasonPhrase()); // OK
				LOGGER.debug("{}", response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonArray = (JsonArray) new JsonParser().parse(EntityUtils.toString(httpEntity));
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonArray;
	}

	public JsonArray getTests(Integer runId) throws IOException {
		CloseableHttpResponse response = null;
		JsonArray jsonArray = null;
		int retry = retryCnt;
		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("get_tests/" + runId).build();
				LOGGER.debug(LoggerServices.build().bannerWrap("Rest URI", request.toString(), '#'));

				HttpGet httpGet = new HttpGet(request);
				httpGet.setConfig(requestConfig);
				httpGet.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				// Get HttpResponse Status
				LOGGER.debug("{}", response.getProtocolVersion()); // HTTP/1.1
				LOGGER.debug("{}", response.getStatusLine().getStatusCode()); // 200
				LOGGER.debug("{}", response.getStatusLine().getReasonPhrase()); // OK
				LOGGER.debug("{}", response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));

				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonArray = (JsonArray) new JsonParser().parse(EntityUtils.toString(httpEntity));
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonArray;
	}

	public JsonArray getPlans(Integer projectId) throws IOException {
		CloseableHttpResponse response = null;
		JsonArray jsonArray = null;
		int retry = retryCnt;
		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("get_plans/" + projectId).build();
				LOGGER.debug(LoggerServices.build().bannerWrap("Rest URI", request.toString(), '#'));

				HttpGet httpGet = new HttpGet(request);
				httpGet.setConfig(requestConfig);
				httpGet.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				// Get HttpResponse Status
				LOGGER.debug("{}", response.getProtocolVersion()); // HTTP/1.1
				LOGGER.debug("{}", response.getStatusLine().getStatusCode()); // 200
				LOGGER.debug("{}", response.getStatusLine().getReasonPhrase()); // OK
				LOGGER.debug("{}", response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonArray = (JsonArray) new JsonParser().parse(EntityUtils.toString(httpEntity));
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonArray;
	}

	public JsonArray getTestCaseTypes() throws IOException {
		CloseableHttpResponse response = null;
		JsonArray jsonArray = null;
		int retry = retryCnt;
		while (retry >= 0) {
			try {
				UriBuilder uriBuilder = UriBuilder.fromPath(serverUrl).path("get_case_types");

				URI request = uriBuilder.build();
				LOGGER.info("{}", request);
				LOGGER.debug(LoggerServices.build().bannerWrap("Rest URI", request.toString(), '#'));

				HttpGet httpGet = new HttpGet(request);
				httpGet.setConfig(requestConfig);
				httpGet.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				// Get HttpResponse Status
				LOGGER.debug("{}", response.getProtocolVersion()); // HTTP/1.1
				LOGGER.debug("{}", response.getStatusLine().getStatusCode()); // 200
				LOGGER.debug("{}", response.getStatusLine().getReasonPhrase()); // OK
				LOGGER.debug("{}", response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonArray = (JsonArray) new JsonParser().parse(EntityUtils.toString(httpEntity));
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonArray;
	}

	public JsonObject getUserByName(String userName, boolean bFailOnNotFound)
			throws IOException, TestRailConfigException {
		JsonArray jsonArray = getUsersByName(userName);

		if (bFailOnNotFound && jsonArray.size() == 0) {
			throw new TestRailConfigException("User not found: " + userName);
		}

		if (jsonArray.size() > 1) {
			throw new TestRailConfigException("User not unique: " + userName);
		}

		return jsonArray.size() == 0 ? null : (JsonObject) jsonArray.get(0);
	}

	public JsonArray getUsersByName(String userName) throws IOException {
		JsonArray jsonArray = new JsonArray();

		JsonArray userArray = getUsers();

		for (JsonElement jsonElement : userArray) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			LOGGER.trace("comparing: {} to: {}", jsonObject.get("name"), userName);
			if (jsonObject.get("name").getAsString().equals(userName)) {
				//TODO checl log msg test on all
				LOGGER.debug("adding user: {}", jsonObject.get("name"));
				jsonArray.add(jsonObject);
			}
		}

		return jsonArray;
	}

	public JsonArray getUsersByEmail(String userEmail) throws IOException {
		JsonArray jsonArray = new JsonArray();

		JsonArray userArray = getUsers();

		for (JsonElement jsonElement : userArray) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			LOGGER.trace("comparing: {} to: {}", jsonObject.get("email"), userName);
			if (jsonObject.get("name").getAsString().equals(userName)) {
				//TODO checl log msg test on all
				LOGGER.debug("adding user: {}", jsonObject.get("name"));
				jsonArray.add(jsonObject);
			}
		}

		return jsonArray;
	}

	public Integer getUserIdByEmail(String userEmail, boolean bFailOnNotFound)
			throws TestRailConfigException, IOException {
		JsonObject jsonObject = getUserByEmail(userEmail, bFailOnNotFound);
		return jsonObject == null ? null : jsonObject.get("id").getAsInt();
	}

	public JsonObject getUserByEmail(String userEmail, boolean bFailOnNotFound)
			throws TestRailConfigException, IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;
		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("get_user_by_email").queryParam("email", userEmail)
						.build();
				LOGGER.debug(LoggerServices.build().bannerWrap("Rest URI", request.toString(), '#'));

				HttpGet httpGet = new HttpGet(request);
				httpGet.setConfig(requestConfig);
				httpGet.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				// Get HttpResponse Status
				LOGGER.debug("{}", response.getProtocolVersion()); // HTTP/1.1
				LOGGER.debug("{}", response.getStatusLine().getStatusCode()); // 200
				LOGGER.debug("{}", response.getStatusLine().getReasonPhrase()); // OK
				LOGGER.debug("{}", response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonObject = (JsonObject) new JsonParser().parse(EntityUtils.toString(httpEntity));
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}
		return jsonObject;
	}

	public JsonArray getUsers() throws IOException {
		CloseableHttpResponse response = null;
		JsonArray jsonArray = null;
		int retry = retryCnt;
		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("get_users").build();
				LOGGER.debug(LoggerServices.build().bannerWrap("Rest URI", request.toString(), '#'));

				HttpGet httpGet = new HttpGet(request);
				httpGet.setConfig(requestConfig);
				httpGet.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				// Get HttpResponse Status
				LOGGER.debug("{}", response.getProtocolVersion()); // HTTP/1.1
				LOGGER.debug("{}", response.getStatusLine().getStatusCode()); // 200
				LOGGER.debug("{}", response.getStatusLine().getReasonPhrase()); // OK
				LOGGER.debug("{}", response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonArray = (JsonArray) new JsonParser().parse(EntityUtils.toString(httpEntity));
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}
		return jsonArray;
	}

	public JsonObject getPlan(Integer planId, boolean bFailOnNotFound) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;
		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("get_plan/" + planId).build();

				LOGGER.debug(LoggerServices.build().bannerWrap("Rest URI", request.toString(), '#'));

				HttpGet httpGet = new HttpGet(request);
				httpGet.setConfig(requestConfig);
				httpGet.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				// Get HttpResponse Status
				LOGGER.debug("{}", response.getProtocolVersion()); // HTTP/1.1
				LOGGER.debug("{}", response.getStatusLine().getStatusCode()); // 200
				LOGGER.debug("{}", response.getStatusLine().getReasonPhrase()); // OK
				LOGGER.debug("{}", response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (bFailOnNotFound && jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonObject = (JsonObject) new JsonParser().parse(EntityUtils.toString(httpEntity));
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	//TODO goes away when convert to builder model
	public Boolean isProjectExists(String projectName) throws IOException, TestRailConfigException {
		boolean exists = false;
		Integer id = getProjectIdByName(projectName, false);
		exists = id != null;
		LOGGER.debug("projectId: {}", id);

		return exists;
	}

	public JsonArray getRunsByName(Integer projectId, String runName) throws IOException {
		JsonArray jsonArray = new JsonArray();

		JsonArray runArray = getRuns(projectId);

		for (JsonElement jsonElement : runArray) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			LOGGER.trace("comparing: {} to: {}", jsonObject.get("name"), runName);
			if (jsonObject.get("name").getAsString().equals(runName)) {
				LOGGER.debug("adding run: {}", jsonObject.get("name"));
				jsonArray.add(jsonObject);
			}
		}

		return jsonArray;
	}

	public Integer getRunIdByName(Integer projectId, String runName, boolean bFailOnNotFound)
			throws TestRailConfigException, IOException {
		JsonObject jsonObject = getRunByName(projectId, runName, bFailOnNotFound);
		return jsonObject == null ? null : jsonObject.get("id").getAsInt();
	}

	public Integer getRunIdByName(String projectName, String runName, boolean bFailOnNotFound)
			throws TestRailConfigException, IOException {
		return getRunIdByName(getProjectIdByName(projectName, true), runName, bFailOnNotFound);
	}

	public JsonObject getRunByName(Integer projectId, String runName, boolean bFailOnNotFound)
			throws IOException, TestRailConfigException {
		JsonArray jsonArray = getRunsByName(projectId, runName);

		if (bFailOnNotFound && jsonArray.size() == 0) {
			throw new TestRailConfigException("Run not found: " + runName);
		}

		if (jsonArray.size() > 1) {
			throw new TestRailConfigException("Run not unique: " + runName);
		}

		return jsonArray.size() == 0 ? null : (JsonObject) jsonArray.get(0);
	}

	//TODO for all??
	public Boolean isRunExists(Integer runId) throws IOException, TestRailConfigException {
		boolean exists = false;
		JsonObject jsonObject = getRun(runId, false);
		exists = jsonObject != null;
		LOGGER.debug("runId: {}", jsonObject);

		return exists;
	}

	public Boolean isRunExists(Integer projectId, String runName) throws IOException, TestRailConfigException {
		boolean exists = false;
		Integer id = getRunIdByName(projectId, runName, false);
		exists = id != null;
		LOGGER.debug("runId: {}", id);

		return exists;
	}

	public JsonArray getSuitesByName(Integer projectId, String suiteName) throws IOException {
		JsonArray jsonArray = new JsonArray();

		JsonArray suiteArray = getSuites(projectId);

		for (JsonElement jsonElement : suiteArray) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			LOGGER.trace("comparing: {} to: {}", jsonObject.get("name"), suiteName);
			if (jsonObject.get("name").getAsString().equals(suiteName)) {
				LOGGER.debug("adding project: {}", jsonObject.get("name"));
				jsonArray.add(jsonObject);
			}
		}

		return jsonArray;
	}

	public JsonObject getSuiteByName(Integer projectId, String suiteName, boolean bFailOnNotFound)
			throws IOException, TestRailConfigException {
		JsonArray jsonArray = getSuitesByName(projectId, suiteName);

		if (bFailOnNotFound && jsonArray.size() == 0) {
			throw new TestRailConfigException("Suite not found: " + suiteName);
		}

		if (jsonArray.size() > 1) {
			throw new TestRailConfigException("Suite not unique: " + suiteName);
		}

		return jsonArray.size() == 0 ? null : (JsonObject) jsonArray.get(0);
	}

	public Integer getSuiteIdByName(Integer projectId, String suiteName, boolean bFailOnNotFound)
			throws TestRailConfigException, IOException {
		JsonObject jsonObject = getSuiteByName(projectId, suiteName, bFailOnNotFound);
		return jsonObject == null ? null : jsonObject.get("id").getAsInt();
	}

	public Boolean isSuiteExists(Integer projectId, String suiteName) throws IOException, TestRailConfigException {
		boolean exists = false;
		Integer id = getSuiteIdByName(projectId, suiteName, false);
		exists = id != null;
		LOGGER.debug("suiteId: {}", id);

		return exists;
	}

	public Boolean isSectionExists(Integer projectId, Integer suiteId, String sectionName)
			throws IOException, TestRailConfigException {
		boolean exists = false;

		Integer id = getSectionIdByName(projectId, suiteId, sectionName, false);
		exists = id != null;
		LOGGER.debug("sectionId: {}", id);

		return exists;
	}

	//TODO do this for all is methods
	public Boolean isSectionExists(Integer projectId, String suiteName, String sectionName)
			throws IOException, TestRailConfigException {
		Integer suiteId = getSuiteIdByName(projectId, suiteName, true);
		Integer sectionId = getSectionIdByName(projectId, suiteId, sectionName, true);
		return isSectionExists(projectId, getSectionIdByName(projectId, suiteId, sectionName, false), sectionName);
	}

	public Boolean isTestCaseExists(Integer testCaseId) throws IOException, TestRailConfigException {
		boolean exists = false;
		JsonObject jsonObject = getTestCase(testCaseId, false);
		exists = jsonObject != null;
		return exists;
	}

	public Boolean isTestExists(Integer runId, String testName) throws IOException, TestRailConfigException {
		boolean exists = false;

		Integer id = getTestIdByName(runId, testName, false);
		exists = id != null;
		LOGGER.debug("testId: {}", id);

		return exists;
	}

	public JsonObject getMileStoneByName(Integer projectId, String mileStoneName, boolean bFailOnNotFound)
			throws IOException, TestRailConfigException {
		JsonArray jsonArray = getMileStonesByName(projectId, mileStoneName);

		if (bFailOnNotFound && jsonArray.size() == 0) {
			throw new TestRailConfigException("MileStone not found: " + mileStoneName);
		}

		if (jsonArray.size() > 1) {
			throw new TestRailConfigException("MileStone not unique: " + mileStoneName);
		}

		return jsonArray.size() == 0 ? null : (JsonObject) jsonArray.get(0);
	}

	public Integer getMileStoneIdByName(Integer projectId, String mileStoneName, boolean bFailOnNotFound)
			throws TestRailConfigException, IOException {
		JsonObject jsonObject = getMileStoneByName(projectId, mileStoneName, bFailOnNotFound);
		return jsonObject == null ? null : jsonObject.get("id").getAsInt();
	}

	public Integer getMileStoneIdByName(String projectName, String mileStoneName, boolean bFailOnNotFound)
			throws TestRailConfigException, IOException {
		JsonObject jsonObject = getMileStoneByName(getProjectIdByName(projectName, true), mileStoneName,
				bFailOnNotFound);
		return jsonObject == null ? null : jsonObject.get("id").getAsInt();
	}

	public JsonObject getSection(Integer sectionId, boolean bFailOnNotFound) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;
		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("get_section/" + sectionId).build();

				LOGGER.debug(LoggerServices.build().bannerWrap("Rest URI", request.toString(), '#'));

				HttpGet httpGet = new HttpGet(request);
				httpGet.setConfig(requestConfig);
				httpGet.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				// Get HttpResponse Status
				LOGGER.debug("{}", response.getProtocolVersion()); // HTTP/1.1
				LOGGER.debug("{}", response.getStatusLine().getStatusCode()); // 200
				LOGGER.debug("{}", response.getStatusLine().getReasonPhrase()); // OK
				LOGGER.debug("{}", response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (bFailOnNotFound && jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonObject = (JsonObject) new JsonParser().parse(EntityUtils.toString(httpEntity));
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	public JsonObject getPlanByName(Integer projectId, String planName, boolean bFailOnNotFound)
			throws IOException, TestRailConfigException {
		JsonArray jsonArray = getPlansByName(projectId, planName);

		if (bFailOnNotFound && jsonArray.size() == 0) {
			throw new TestRailConfigException("Suite not found: " + planName);
		}

		if (jsonArray.size() > 1) {
			throw new TestRailConfigException("Suite not unique: " + planName);
		}

		return jsonArray.size() == 0 ? null : (JsonObject) jsonArray.get(0);
	}

	public Integer getPlanIdByName(Integer projectId, String planName, boolean bFailOnNotFound)
			throws TestRailConfigException, IOException {
		JsonObject jsonObject = getPlanByName(projectId, planName, bFailOnNotFound);
		return jsonObject == null ? null : jsonObject.get("id").getAsInt();
	}

	public JsonArray getPlansByName(Integer projectId, String planName) throws IOException {
		JsonArray jsonArray = new JsonArray();

		JsonArray planArray = getPlans(projectId);

		for (JsonElement jsonElement : planArray) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			LOGGER.trace("comparing: {} to: {}", jsonObject.get("name"), planName);
			if (jsonObject.get("name").getAsString().equals(planName)) {
				LOGGER.debug("adding plan: {}", jsonObject.get("name"));
				jsonArray.add(jsonObject);
			}
		}

		return jsonArray;
	}

	public JsonObject getMileStone(Integer mileStoneId, boolean bFailOnNotFound) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;
		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("get_milestone/" + mileStoneId).build();

				LOGGER.debug(LoggerServices.build().bannerWrap("Rest URI", request.toString(), '#'));

				HttpGet httpGet = new HttpGet(request);
				httpGet.setConfig(requestConfig);
				httpGet.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				// Get HttpResponse Status
				LOGGER.debug("{}", response.getProtocolVersion()); // HTTP/1.1
				LOGGER.debug("{}", response.getStatusLine().getStatusCode()); // 200
				LOGGER.debug("{}", response.getStatusLine().getReasonPhrase()); // OK
				LOGGER.debug("{}", response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));

				if (bFailOnNotFound && jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonObject = (JsonObject) new JsonParser().parse(EntityUtils.toString(httpEntity));
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	public JsonArray getMileStonesByName(Integer projectId, String mileStoneName) throws IOException {
		JsonArray jsonArray = new JsonArray();

		JsonArray mileStoneArray = getMileStones(projectId);

		for (JsonElement jsonElement : mileStoneArray) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			LOGGER.trace("comparing: {} to: {}", jsonObject.get("name"), mileStoneName);
			if (jsonObject.get("name").getAsString().equals(mileStoneName)) {
				LOGGER.debug("adding project: {}", jsonObject.get("name"));
				jsonArray.add(jsonObject);
			}
		}

		return jsonArray;
	}

	public JsonObject getRun(Integer runId, boolean bFailOnNotFound) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;
		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("get_run/" + runId).build();

				LOGGER.debug(LoggerServices.build().bannerWrap("Rest URI", request.toString(), '#'));

				HttpGet httpGet = new HttpGet(request);
				httpGet.setConfig(requestConfig);
				httpGet.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				// Get HttpResponse Status
				LOGGER.debug("{}", response.getProtocolVersion()); // HTTP/1.1
				LOGGER.debug("{}", response.getStatusLine().getStatusCode()); // 200
				LOGGER.debug("{}", response.getStatusLine().getReasonPhrase()); // OK
				LOGGER.debug("{}", response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));

				if (bFailOnNotFound && jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonObject = (JsonObject) new JsonParser().parse(EntityUtils.toString(httpEntity));
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	public JsonArray getTestCasesByName(Integer projectId, Integer suiteId, Integer sectionId, String testCaseName,
			boolean bFailOnNotFound) throws IOException {
		JsonArray jsonArray = new JsonArray();

		JsonArray testCaseArray = getTestCases(projectId, suiteId, sectionId);

		for (JsonElement jsonElement : testCaseArray) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			LOGGER.trace("comparing: {} to: {}", jsonObject.get("name"), testCaseName);
			if (jsonObject.get("name").getAsString().equals(testCaseName)) {
				LOGGER.debug("adding project: {}", jsonObject.get("name"));
				jsonArray.add(jsonObject);
			}
		}

		return jsonArray;
	}

	public JsonObject getTestCaseByName(Integer projectId, Integer suiteId, Integer sectionId, String testCaseName,
			boolean bFailOnNotFound) throws IOException, TestRailConfigException {
		JsonArray jsonArray = getTestCasesByName(projectId, suiteId, sectionId, testCaseName, bFailOnNotFound);

		if (bFailOnNotFound && jsonArray.size() == 0) {
			throw new TestRailConfigException("TestCase not found: " + testCaseName);
		}

		if (jsonArray.size() > 1) {
			throw new TestRailConfigException("TestCase not unique: " + testCaseName);
		}

		return jsonArray.size() == 0 ? null : (JsonObject) jsonArray.get(0);
	}

	public Integer getTestCaseIdByName(Integer projectId, Integer suiteId, Integer sectionId, String testCaseName,
			boolean bFailOnNotFound) throws TestRailConfigException, IOException {
		JsonObject jsonObject = getTestCaseByName(projectId, suiteId, sectionId, testCaseName, bFailOnNotFound);
		return jsonObject == null ? null : jsonObject.get("id").getAsInt();
	}

	/*
		Adds a new test result, comment or assigns a test (for a test run and case combination). 
		It's recommended to use add_results_for_cases instead if you plan to add results for multiple test cases.
	
		The difference to add_result is that this method expects a test run + test case instead of a test. 
		In TestRail, tests are part of a test run and the test cases are part of the related test suite. 
		So, when you create a new test run, TestRail creates a test for each test case found in the test suite of the run. 
		You can therefore think of a test as an “instance” of a test case which can have test results, comments and a test status. 
		Please also see TestRail's getting started guide for more details about the differences between test cases and tests.
	*/
	private JsonObject postTestResults(Integer runId, Integer testCaseId, String json) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;

		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("add_result_for_case/" + runId + "/" + testCaseId)
						.build();
				HttpPost httpPost = new HttpPost(request);
				httpPost.setConfig(requestConfig);
				httpPost.addHeader("Content-Type", "application/json");
				httpPost.setEntity(new StringEntity(json));
				response = httpClient.execute(httpPost);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				//TODO consistancy on all like other approach

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonObject = (JsonObject) new JsonParser().parse(EntityUtils.toString(httpEntity));

				break;
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	private JsonArray getSectionsByName(Integer projectId, Integer suiteId, String sectionName) throws IOException {
		JsonArray jsonArray = new JsonArray();

		JsonArray sectionArray = getSections(projectId, suiteId);

		for (JsonElement jsonElement : sectionArray) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			LOGGER.trace("comparing: {} to: {}", jsonObject.get("name"), sectionName);
			if (jsonObject.get("name").getAsString().equals(sectionName)) {
				LOGGER.debug("adding project: {}", jsonObject.get("name"));
				jsonArray.add(jsonObject);
			}
		}

		return jsonArray;
	}

	public JsonObject getSectionByName(Integer projectId, Integer suiteId, String sectionName, boolean bFailOnNotFound)
			throws IOException, TestRailConfigException {
		JsonArray jsonArray = getSectionsByName(projectId, suiteId, sectionName);

		if (bFailOnNotFound && jsonArray.size() == 0) {
			throw new TestRailConfigException("Section not found: " + sectionName);
		}
		//TODO make sure all strings are correct
		if (jsonArray.size() > 1) {
			throw new TestRailConfigException("Section not unique: " + sectionName);
		}

		return jsonArray.size() == 0 ? null : (JsonObject) jsonArray.get(0);
	}

	public Integer getSectionIdByName(Integer projectId, Integer suiteId, String sectionName, boolean bFailOnNotFound)
			throws TestRailConfigException, IOException {
		JsonObject jsonObject = getSectionByName(projectId, suiteId, sectionName, true);
		return jsonObject == null ? null : jsonObject.get("id").getAsInt();
	}

	public Boolean isTestCaseExists(Integer projectId, Integer suiteId, Integer sectionId, String testCaseName)
			throws IOException, TestRailConfigException {
		boolean exists = false;
		Integer id = getTestCaseIdByName(projectId, suiteId, sectionId, testCaseName, false);
		exists = id != null;
		LOGGER.debug("projectId: {}", id);

		return exists;
	}

	public JsonObject getTest(Integer testId, boolean bFailOnNotFound) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;
		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("get_test/" + testId).build();

				LOGGER.debug(LoggerServices.build().bannerWrap("Rest URI", request.toString(), '#'));

				HttpGet httpGet = new HttpGet(request);
				httpGet.setConfig(requestConfig);
				httpGet.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				// Get HttpResponse Status
				LOGGER.debug("{}", response.getProtocolVersion()); // HTTP/1.1
				LOGGER.debug("{}", response.getStatusLine().getStatusCode()); // 200
				LOGGER.debug("{}", response.getStatusLine().getReasonPhrase()); // OK
				LOGGER.debug("{}", response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (bFailOnNotFound && jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonObject = (JsonObject) new JsonParser().parse(EntityUtils.toString(httpEntity));
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	public Boolean isMileStoneExists(Integer projectId, String mileStoneName)
			throws IOException, TestRailConfigException {
		boolean exists = false;
		Integer id = getMileStoneIdByName(projectId, mileStoneName, false);
		if (id != null) {
			exists = true;
		}
		return exists;
	}

	public Boolean isMileStoneExists(Integer mileStoneId) throws IOException, TestRailConfigException {
		boolean exists = false;
		JsonObject jsonObject = getMileStone(mileStoneId, false);
		if (jsonObject != null) {
			exists = true;
		}
		return exists;
	}

	public JsonObject updateTestCase(Integer testCaseId, String json) throws IOException {
		CloseableHttpResponse response = null;
		JsonObject jsonObject = null;
		int retry = retryCnt;

		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("update_case/" + testCaseId).build();
				HttpPost httpPost = new HttpPost(request);
				httpPost.setConfig(requestConfig);
				httpPost.addHeader("Content-Type", "application/json");
				httpPost.setEntity(new StringEntity(json));
				response = httpClient.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonObject = jsonElement.getAsJsonObject();

				break;
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonObject;
	}

	public Integer getTestCaseTypeIdByName(String testCaseTypeName, boolean bFailOnNotFound)
			throws IOException, TestRailConfigException {
		List<Integer> testCaseIdList = new ArrayList<Integer>();
		JsonArray testCaseTypeArray = getTestCaseTypes();
		LOGGER.trace("TestCaseType: {}", testCaseTypeArray);

		for (JsonElement jsonElement : testCaseTypeArray) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			LOGGER.trace("comparing: {} to: {}", jsonObject.get("name"), testCaseTypeName);
			if (jsonObject.get("name").getAsString().equals(testCaseTypeName)) {
				LOGGER.debug("adding project: {}", jsonObject.get("name"));
				testCaseIdList.add(jsonObject.get("id").getAsInt());
			}
		}

		if (bFailOnNotFound && testCaseIdList.size() == 0) {
			throw new TestRailConfigException("TestCaseId: " + testCaseTypeName + "\n" + testCaseTypeArray);
		}

		if (testCaseIdList.size() > 1) {
			throw new TestRailConfigException("TestCaseId name not unique: " + testCaseTypeName);
		}

		return testCaseIdList.get(0);
	}

	public Integer getUserIdByName(String userName, boolean bFailOnNotFound)
			throws TestRailConfigException, IOException {
		JsonObject jsonObject = getUserByName(userName, bFailOnNotFound);
		return jsonObject == null ? null : jsonObject.get("id").getAsInt();
	}

	//TODO move these into Test builder
	public JsonArray getTestsByName(Integer runId, String testName, boolean bFailOnNotFound)
			throws TestRailConfigException, IOException {
		JsonArray jsonArray = new JsonArray();

		JsonArray testCaseArray = getTests(runId);

		for (JsonElement jsonElement : testCaseArray) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			LOGGER.trace("comparing: {} to: {}", jsonObject.get("name"), testName);
			if (jsonObject.get("name").getAsString().equals(testName)) {
				LOGGER.debug("adding project: {}", jsonObject.get("name"));
				jsonArray.add(jsonObject);
			}
		}

		return jsonArray;
	}

	public JsonObject getTestByName(Integer runId, String testName, boolean bFailOnNotFound)
			throws IOException, TestRailConfigException {
		JsonArray jsonArray = getTestsByName(runId, testName, bFailOnNotFound);

		if (bFailOnNotFound && jsonArray.size() == 0) {
			throw new TestRailConfigException("TestCase not found: " + testName);
		}

		if (jsonArray.size() > 1) {
			throw new TestRailConfigException("TestCase not unique: " + testName);
		}

		return jsonArray.size() == 0 ? null : (JsonObject) jsonArray.get(0);
	}

	public Integer getTestIdByName(Integer runId, String testName, boolean bFailOnNotFound)
			throws TestRailConfigException, IOException {
		JsonObject jsonObject = getTestByName(runId, testName, bFailOnNotFound);
		return jsonObject == null ? null : jsonObject.get("id").getAsInt();
	}

	public JsonArray getTest(Integer testId) throws IOException {
		CloseableHttpResponse response = null;
		JsonArray jsonArray = null;
		int retry = retryCnt;
		while (retry >= 0) {
			try {
				URI request = UriBuilder.fromPath(serverUrl).path("get_test/" + testId).build();
				LOGGER.debug(LoggerServices.build().bannerWrap("Rest URI", request.toString(), '#'));

				HttpGet httpGet = new HttpGet(request);
				httpGet.setConfig(requestConfig);
				httpGet.addHeader("Content-Type", "application/json");
				response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
				}

				// Get HttpResponse Status
				LOGGER.debug("{}", response.getProtocolVersion()); // HTTP/1.1
				LOGGER.debug("{}", response.getStatusLine().getStatusCode()); // 200
				LOGGER.debug("{}", response.getStatusLine().getReasonPhrase()); // OK
				LOGGER.debug("{}", response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity httpEntity = response.getEntity();

				JsonElement jsonElement = new JsonParser().parse(EntityUtils.toString(httpEntity));
				if (jsonElement instanceof JsonNull) {
					throw new IOException("JsonNull");
				}

				jsonArray = (JsonArray) new JsonParser().parse(EntityUtils.toString(httpEntity));
			} catch (IOException e) {
				if (e.getMessage().contains("HTTP 429")) {
					if (retry == 0) {
						throw new IOException("timeout after: " + retry + 1 + " attempts");
					} else {
						LOGGER.debug("server busy...sleeping: " + retrySleepInterval + "ms");
						try {
							Thread.sleep(retrySleepInterval);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					throw e;
				}
			} finally {
				if (response != null) {
					response.close();
				}
				retry--;
			}
		}

		return jsonArray;
	}

	public Project.Builder getProjectBuilder(String projectName) {
		return Project.builder(this, projectName);
	}

	public Plan.Builder getPlanBuilder(String planName) {
		return Plan.builder(this, planName);
	}

	public Run.Builder getRunBuilder(String projectName, String runName) {
		return Run.builder(this, projectName, runName);
	}

	public MileStone.Builder getMileStoneBuilder(String mileStoneName) {
		return MileStone.builder(this, mileStoneName);
	}

	public Suite.Builder getSuiteBuilder(String suiteName) {
		return Suite.builder(this, suiteName);
	}

	public Section.Builder getSectionBuilder(String sectionName) {
		return Section.builder(this, sectionName);
	}

	public TestCase.Builder getTestCaseBuilder(String testCaseName) {
		return TestCase.builder(this, testCaseName);
	}

	public Results.Builder getResultsBuilder(String projectName) {
		return Results.builder(this, projectName);
	}

	public static void main(String[] args) {
		try {
			URI request = UriBuilder.fromPath("http://10.7.3.11/testrail").path("get_cases/2").queryParam("suite_id", 3)
					.build();
			LOGGER.info("{}", request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
