package io.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.jdbc.core.JdbcTemplate;

@EnableBinding(Sink.class)
public class DummySink {
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	String sql = "INSERT INTO test (msg) VALUES (?)";
	
	@StreamListener(Sink.INPUT)
    public void log(String message) {
        jdbcTemplate.update(sql, message);
    }

}
