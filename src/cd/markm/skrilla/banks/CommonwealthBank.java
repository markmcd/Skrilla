package cd.markm.skrilla.banks;

import java.util.ArrayList;
import java.util.Currency;
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
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.Span;
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
		
		try {
			// first we load up the form page & rip out all the internal variables 
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet loginpage = new HttpGet(LOGIN_PAGE);
			
			HttpResponse response = client.execute(loginpage);
			Log.i(TAG, "Executed, status: "+response.getStatusLine());
			// TODO some kind of status checking if we need it
			
			HttpEntity body = response.getEntity();
			String content = Util.convertStreamToString(body.getContent());
			Log.i(TAG, "Content length is "+content.length()+" bytes.");
			
			// TODO handle maintenance mode - see misc/CBAmaintmode.txt
			
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
			body = response.getEntity();
			
			// TODO handle failed login
			
			Log.i(TAG, "Submitted, status code: "+response.getStatusLine());
			
			content = Util.convertStreamToString(body.getContent());
			Log.d(TAG, "Body length is: "+content.length());
			
			// the portfolio details are in table#MyPortfolioGrid1_a, will need
			// to pull out & process each of the rows by hand
			
			parser = new Parser(content);
			// these nodes should be the first <TD> in each row where there
			// is an account being specified (i.e. no headers/footers)
			nodes = parser.extractAllNodesThatMatch(
				new AndFilter(
					new HasParentFilter(
						new HasAttributeFilter("id", "MyPortfolioGrid1_a"),
						true),
					new AndFilter(
						new TagNameFilter("td"),
						new HasAttributeFilter("class", "NicknameField"))));
			
			for (int i = 0; i < nodes.size(); i++) {
				Node n = nodes.elementAt(i);
				Account ac = new Account();
				
				// currency is fixed for CBA - TODO constant this
				ac.setUnit(Currency.getInstance("AUD"));
				
				// account name
				LinkTag a = (LinkTag) n.getFirstChild().getFirstChild();
				ac.setName(a.getLinkText());
				
				// identifier (account number)
				Node actnoCell = n.getNextSibling().getNextSibling();
				Span span = (Span) actnoCell.getFirstChild();
				ac.setIdentifier(span.getStringText());
				
				// balance
				Span balanceCell = (Span) actnoCell.getNextSibling()
					.getFirstChild().getNextSibling();
				ac.setBalance(Double.valueOf(balanceCell.getStringText()));
				
				Log.i(TAG, "Adding account id["+ ac.getIdentifier() +"] type["
						+ ac.getName() +"] balance["+ ac.getBalance() +"]");
				accounts.add(ac);
			}

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
