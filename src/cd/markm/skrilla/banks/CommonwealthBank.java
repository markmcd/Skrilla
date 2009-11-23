package cd.markm.skrilla.banks;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.FormTag;
import org.htmlparser.tags.InputTag;
import org.htmlparser.util.NodeList;

import android.util.Log;

import cd.markm.skrilla.Account;
import cd.markm.skrilla.AuthDetails;
import cd.markm.skrilla.FinancialInstitution;
import cd.markm.skrilla.UsernamePasswordAuth;
import cd.markm.skrilla.Util;

public class CommonwealthBank extends FinancialInstitution {
	private static final String TAG = CommonwealthBank.class.getSimpleName();
	
	private static final String LOGIN_PAGE = 
		"https://www3.netbank.commbank.com.au/netbank/bankmain";
	private static final String FORM_NAME = "LOGINForm";
	private static final String LOGIN_FIELD = "USER_LOGON_NAME";
	private static final String PASSWORD_FIELD = "PASSWORD";
	
	public List<Account> getAccounts(AuthDetails ad) {
		if (ad instanceof UsernamePasswordAuth) {
			return getAccounts((UsernamePasswordAuth) ad);
		}
		else {
			Log.e(TAG, "Unknown auth type - only u/p supported at present");
			return null;
		}
	}
	
	public List<Account> getAccounts(UsernamePasswordAuth upa) {
		List<Account> accounts = new ArrayList<Account>();
		
		// first we load up the form page & rip out all the internal variables 
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet loginpage = new HttpGet(LOGIN_PAGE);
		
		try {
			HttpResponse response = client.execute(loginpage);
			Log.i(TAG, "Executed, status: "+response.getStatusLine());
			// TODO some kind of status checking if we need it
			
			HttpEntity body = response.getEntity();
			String content = Util.convertStreamToString(body.getContent());
			
			Parser parser = new Parser(content);
			
			// find nodes where:
			//    tag == input && parent == (tag == form && name == LOGINForm)
			// or tag == form && name = LOGINForm
			// we have to get them all in one hit as we can't do multiple passes
			NodeFilter formNode = new AndFilter(
				new TagNameFilter("form"),
				new HasAttributeFilter("name", FORM_NAME)
			);
			NodeFilter hiddenInput = new AndFilter(
				new TagNameFilter("input"),
				new HasAttributeFilter("type", "hidden")
			); 
			NodeList nodes = parser.extractAllNodesThatMatch(new OrFilter(
				new AndFilter(hiddenInput, new HasParentFilter(formNode)),
				formNode
			));
			
			NodeList inputs = nodes.extractAllNodesThatMatch(hiddenInput);
			NodeList form = nodes.extractAllNodesThatMatch(formNode);
			
			Log.i(TAG, "Found "+nodes.size()+" nodes.");
			
			// build the list of values to submit to the form
			List<NameValuePair> formVals = new ArrayList<NameValuePair>();
			
			for (int i = 0; i < inputs.size(); i++) {
				Node n = inputs.elementAt(i);
				if (n instanceof InputTag) {
					InputTag it = (InputTag) n;
					String name = it.getAttribute("name");
					String value = it.getAttribute("value");
					Log.d(TAG, "saving form value "+name+"="+value);
					formVals.add(new BasicNameValuePair(name, value));
				}
				else {
					Log.w(TAG, "Skipping unknown node: "+n.toHtml());
				}
			}
			
			// add the login credentials
			// TODO load from db
			formVals.add(new BasicNameValuePair(LOGIN_FIELD, "123"));
			formVals.add(new BasicNameValuePair(PASSWORD_FIELD, "abc"));
			
			// now get the form submission URL
			String action = "";
			if (form.size() > 0) {
				FormTag f = (FormTag) form.elementAt(0);
				action = f.getAttribute("action");
			}
			else {
				// TODO handle this better
				Log.e(TAG, "No form node found... that's pretty bad");
			}
			
			// and submit the form back
			HttpPost post = new HttpPost(action);
			post.setEntity(new UrlEncodedFormEntity(formVals));
			response = client.execute(post);
			HttpEntity entity = response.getEntity();
			
			Log.i(TAG, "Submitted, status code: "+response.getStatusLine());
		} catch (Exception e) {
			Log.e(TAG, "Unhandled exception when loading CBA accounts", e);
		}
		
		
		return accounts;
	}

	@Override
	public AuthDetails authenticationDetails() {
		return new UsernamePasswordAuth();
	}

	@Override
	public String displayName() {
		return "Commonwealth Bank";
	}
}
