package org.jin.httpclient;

import static org.junit.Assert.*;

import org.jin.httpclient.exception.FetchException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class DefaultClientTest {

	private Client client = null;
	
	@Before
	public void setUp() throws Exception {
		client = new DefaultClient();
	}

	@Test(timeout=3000)
	public void testGetStringMapOfStringStringCharset(){
		try {
			Response response = client.get("http://www.baidu.com");
//			assertEquals(response, null);
			assertNotEquals(response, null);
			System.out.println(response.asHtml());
		} catch (FetchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Ignore("testPostStringMapOfStringStringHttpEntity")
	@Test
	public void testPostStringMapOfStringStringHttpEntity() {
		fail("Not yet implemented");
	}

	@Test
	public void testDefaultClient() {
		fail("Not yet implemented");
	}

}
