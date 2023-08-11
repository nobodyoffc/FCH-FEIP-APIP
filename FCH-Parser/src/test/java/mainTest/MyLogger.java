package mainTest;


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyLogger {
	
	private static final Logger log = LoggerFactory.getLogger(MyLogger.class);
	
	@Test
	public void testLog() {
		log.error("test error log into file.");
		log.debug("This is a debug log");
		log.info("Thist is info log");
	}
}
