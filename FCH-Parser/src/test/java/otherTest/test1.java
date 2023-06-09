package otherTest;

import java.io.IOException;
import java.util.Scanner;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.junit.jupiter.api.Test;


public class test1 {
	
	@Test
	public void httpsClientClose() throws ElasticsearchException, IOException {
		
		System.out.println("Creating a client with authentication...");
				
	    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	    
	    String username = "elastic";
		String password = "*eQeW7gdWfaH*uPjq-D1";
		String host = "192.168.31.193";
		int port	= 9200;
		credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
		RestClient restClient = RestClient.builder(new HttpHost(host, port,"https"))
			.setHttpClientConfigCallback(h ->h.setDefaultCredentialsProvider(credentialsProvider))
			.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
	        	@Override
				public RequestConfig.Builder customizeRequestConfig(
						RequestConfig.Builder requestConfigBuilder) {
					return requestConfigBuilder.setConnectTimeout(5000 * 1000) // 连接超时（默认为1秒）
							.setSocketTimeout(6000 * 1000);// 套接字超时（默认为30秒）//更改客户端的超时限制默认30秒现在改为100*1000分钟
				}
	        })
			.build();
	    
		// Create the transport with a Jackson mapper
		RestClientTransport transport = new RestClientTransport(
				restClient, new JacksonJsonpMapper());
	
		// And create the API client
		ElasticsearchClient client = new ElasticsearchClient(transport);
		
		System.out.println("Client has been created: "+client.toString());
	}
	
	
	public void choose() {

		System.out.println("\n\nInput the number you want to do:\n");
		
		Scanner sc = new Scanner(System.in);
		int choice=0;	
		while(!sc.hasNextInt()){
			sc.next();
		}
		choice = sc.nextInt();
		System.out.println("Input one of the integers shown above."+choice);
		sc.close();
	}
	

	public void test() {
		System.out.println("\n\nInput the number you want to do:\n");
	    Scanner s = new Scanner(System.in);
	    int sum = 0;
	    System.out.println("input:"+sum);
	    while(s.hasNextInt()) {
	        sum += s.nextInt();
	    }
	    System.out.println(sum);
	    s.close();
	}
	
	
	
}
