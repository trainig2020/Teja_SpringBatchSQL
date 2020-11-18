package com.writer;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

@Component
public class DatabaseWriter implements ItemWriter<String> {

	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired
	public DataSource dataSource;

	@Override
	public void write(List<? extends String> items) throws Exception {
		for (String query : items) {
			System.out.println("Query is : " + query);
			Connection conn = DataSourceUtils.getConnection(dataSource);
			Statement stmt = conn.createStatement();
			 try{
				  
				  stmt.executeUpdate(query);
				  
				  }catch(Exception e)
				  {
					 
					  System.out.println(" Error occured ");
				  }
		}
	}

}
