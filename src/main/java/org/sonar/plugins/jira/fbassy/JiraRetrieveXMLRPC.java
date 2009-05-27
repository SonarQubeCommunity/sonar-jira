package org.codehaus.sonar;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

//use in local with void

@SuppressWarnings({"unused","unchecked"})
public class JiraRetrieveXMLRPC implements JiraConstantes {
	public static Object[] projects;
	public static XmlRpcClient rpcClient = new XmlRpcClient();
	public static XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
	public static String loginToken;
	public static Vector loginParams = new Vector(2);
	public static Vector loginTokenVector = new Vector(1);

	public static void main(String[] args) throws KeyManagementException,
			NoSuchAlgorithmException {
		try {
			acceptCertificat();
			login();
			

			ArrayList maListe = testAllIssueWithVersion("ProjectName", "0.0.1");
			System.out.println(maListe + " was : " + maListe.size());
			
			logout();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
	}
	
	private static void logout() throws XmlRpcException {
		Boolean bool = (Boolean) rpcClient.execute("jira1.logout",
				loginTokenVector);
		System.out.println("Logout successful: " + bool);
	}
	
	private static void login() throws XmlRpcException, MalformedURLException {
		System.setProperty("https.proxyHost", "proxy");
		System.setProperty("https.proxyPort", "3128");

		config.setServerURL(new URL(JIRA_URI + RPC_PATH));
		rpcClient.setConfig(config);

		// Login and retrieve logon token
		loginParams.add(USER_NAME);
		loginParams.add(PASSWORD);

		loginToken = (String) rpcClient.execute("jira1.login", loginParams);
		System.out.println("Login successful");
		// Retrieve projects
		loginTokenVector.add(loginToken);
	}

	private static void testProjet() throws XmlRpcException {
		try {
			projects = (Object[]) rpcClient.execute(
					"jira1.getProjectsNoSchemes", loginTokenVector);
			System.out.println("getProjects Sucess");
			// Print projects
			for (int i = 0; i < projects.length; i++) {
				Map project = (Map) projects[i];
				System.out.println("ID: " + project.get("id") + "\tKEY: "
						+ project.get("key") + "\tNAME: " + project.get("name")
						+ "\tLEAD: " + project.get("lead"));

			}
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
	}

	private static void testVersion(String aProjectKey) throws XmlRpcException {
		System.out
				.println("TEST: " + aProjectKey + " => Retrieve avaible version");
		Vector logTokenVector = new Vector(2);
		String mavar = aProjectKey;

		logTokenVector.add(loginToken);
		logTokenVector.add(mavar);

		projects = (Object[]) rpcClient.execute("jira1.getVersions",
				logTokenVector);

		// Print projects
		for (int i = 0; i < projects.length; i++) {
			Map project = (Map) projects[i];
			System.out.println("ID: " + project.get("id") + "\tNAME: "
					+ project.get("name"));
		}
	}

	private static void testAllIssue(String aProjectKey) throws XmlRpcException {
		System.out.println("TEST: " + aProjectKey + " => Retrieve issue");
		Vector MyTokenVector = new Vector(4);
		Vector mavar2 = new Vector(1);
		mavar2.add(aProjectKey);
		String mavar3 = ("");
		int nb = 100;

		MyTokenVector.add(loginToken);
		MyTokenVector.add(mavar2);
		MyTokenVector.add(mavar3);
		MyTokenVector.add(nb);

		projects = (Object[]) rpcClient.execute(
				"jira1.getIssuesFromTextSearchWithProject", MyTokenVector);

		for (int i = 0; i < projects.length; i++) {
			Map project = (Map) projects[i];
			System.out.println("ID: " + project.get("id")
					+ "\tAFFFECTSVERSION: " + project.get("affectsVersions")
					+ "\tKEY: " + project.get("key"));
			for (Object o : (Object[]) project.get("affectsVersions")) {
				System.out.println(o);
			}
		}

	}

	private static ArrayList testAllIssueWithVersion(String aProjectKey,
			String aVersionName) throws XmlRpcException {
		ArrayList myList = new ArrayList();
		//System.out.println("TEST: " + aProjectKey + " => Retrieve "+ aVersionName);
		Vector MyTokenVector = new Vector(4);
		Vector mavar2 = new Vector(1);
		mavar2.add(aProjectKey);
		String mavar3 = ("");
		int nb = 100;
		String uneVersion = new String();
		MyTokenVector.add(loginToken);
		MyTokenVector.add(mavar2);
		MyTokenVector.add(mavar3);
		MyTokenVector.add(nb);

		projects = (Object[]) rpcClient.execute(
				"jira1.getIssuesFromTextSearchWithProject", MyTokenVector);

		for (int i = 0; i < projects.length; i++) {
			Map project = (Map) projects[i];
			
			uneVersion = VersionAffecte(project.get("key").toString());
			//System.out.println(uneVersion +" is " +aVersionName);
			if (uneVersion.equals(aVersionName)) {
				//System.out.println("ID: " + project.get("id")+ "\tAFFFECTSVERSION: "+ project.get("affectsVersions") + "\tKEY: "+ project.get("key")+ " " + uneVersion);
				myList.add(project.get("key"));
			}
		}
		return myList;
	}

	private static void testIssue(String aIssueKey) throws XmlRpcException {
		System.out.println("issue information : "
				+ aIssueKey);
		Vector TokenVector = new Vector(2);
		String mavar4 = (aIssueKey);
		TokenVector.add(loginToken);
		TokenVector.add(mavar4);

		HashMap result = (HashMap) rpcClient.execute("jira1.getIssue",
				TokenVector);
		Iterator i = result.keySet().iterator();
		String clef;

		while (i.hasNext()) {
			clef = (String) i.next();
			Object value = result.get(clef);
			if (value instanceof String) {
				System.out.println(value);
			} else if (value instanceof Object[]) {
				for (Object o : (Object[]) value) {
					System.out.println(o);
				}
			}
		}
	}

	static String VersionAffecte(String aIssueKey) throws XmlRpcException {
		//System.out.println("Retrieve : " + aIssueKey);
		Vector TokenVector = new Vector(2);
		String mavar4 = (aIssueKey);
		TokenVector.add(loginToken);
		TokenVector.add(mavar4);

		HashMap result = (HashMap) rpcClient.execute("jira1.getIssue",
				TokenVector);

		Iterator i = result.keySet().iterator();
		String clef = new String();
		HashMap LigneAffect = new HashMap();
		;
		String versionAffect = new String();
		try {
		while (i.hasNext()) {
			clef = (String) i.next();
			Object value = result.get(clef);
			if (value instanceof Object[]) {
				
				for (Object o : (Object[]) value) {
					LigneAffect = (HashMap) o;
					if (Integer.valueOf(LigneAffect.get("sequence").toString()) == 1)
						versionAffect = LigneAffect.get("name").toString();
				}
			}
		}
		} catch (Exception e) {
			// handle exception
		}
		return versionAffect;
	}

	private static void acceptCertificat() throws NoSuchAlgorithmException,
			KeyManagementException {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] {

		new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs,
					String authType) {
				// Trust always
			}

			public void checkServerTrusted(X509Certificate[] certs,
					String authType) {
				// Trust always
			}
		} };

		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		// Create empty HostnameVerifier
		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		};

		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}
}