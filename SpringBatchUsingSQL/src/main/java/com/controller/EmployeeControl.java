
package com.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.config.SQLConfig;

import javaxt.io.Directory;
import javaxt.io.Directory.Event;

@RestController
public class EmployeeControl {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	@Qualifier("autoScheJob")
	Job job;
	
	@Autowired
	@Qualifier("manualScheJob")
	Job job2;
	
	static Resource[] res;
	
	
	public static Resource[] getRes() {
		return res;
	}

	public static void setRes(Resource[] res) {
		EmployeeControl.res = res;
	}

	@RequestMapping("/")
	public ModelAndView homePage() {
		return new ModelAndView("home");
	}

	

	@RequestMapping("/auto")
	public ModelAndView autoScheduling() throws Exception {
		ModelAndView modelAndView = new ModelAndView("home");
		Map<String, JobParameter> maps = new HashMap<>();
		maps.put("time", new JobParameter(System.currentTimeMillis()));
		JobParameters parameters = new JobParameters(maps);
		JobExecution jobExecution = jobLauncher.run(job, parameters);
		System.out.println("JobExecution: " + jobExecution.getStatus());
  
  
		Directory folder = new Directory("D:\\Teja\\SqlQueries");
		Directory folderCopy = new Directory("D:\\Teja\\SqLQueriesCopy");
		try {
			sync(folder, folderCopy);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (jobExecution.isRunning()) {
			System.out.println("...");
		}

		return modelAndView;
	}

	@SuppressWarnings("rawtypes")
	private void sync(Directory source, Directory destination) throws Exception {

		
		List events = source.getEvents();
		while (true) {
			Event event;
			// Wait for new events to be added to the que
			synchronized (events) {
				while (events.isEmpty()) {
					try {
						
						events.wait();
						
					} catch (InterruptedException e) {
					}
				}
				event = (Event) events.remove(0);
			}
			int eventID = event.getEventID();
			System.out.println("EventId : " + eventID);
			if (eventID == Event.DELETE) {
				// Build path to the file in the destination directory
				String path = destination + "\\" + event.getFile().substring(source.toString().length());
				System.out.println("Deleted file is " + path);
				// Delete the file/directory
				new java.io.File(path).delete();
			} else {
				// Check if the event is associated with a file or directory so
				// we can use the JavaXT classes to create or modify the target.
				java.io.File obj = new java.io.File(event.getFile());
				if (obj.isDirectory()) {
					javaxt.io.Directory dir = new javaxt.io.Directory(obj);
					javaxt.io.Directory dest = new javaxt.io.Directory(
							destination + dir.toString().substring(source.toString().length()));
					switch (eventID) {
					case (Event.CREATE):
						//dir.copyTo(dest, true);
						System.out.println("event creation");
						break;
					case (Event.MODIFY):
						System.out.println("event modification");
						break; // TODO
					case (Event.RENAME): {
						javaxt.io.Directory orgDir = new javaxt.io.Directory(event.getOriginalFile());
						dest = new javaxt.io.Directory(
								destination + orgDir.toString().substring(source.toString().length()));
						dest.rename(dir.getName());
						System.out.println("renaming");
						break;
					}
					}
				} else {
					javaxt.io.File file = new javaxt.io.File(obj);
					javaxt.io.File dest = new javaxt.io.File(
							destination + file.toString().substring(source.toString().length()));

					switch (eventID) {
					case (Event.CREATE):
						event.getFile();
						System.out.println("file name is " + event.getFile());
						
						Map<String, JobParameter> maps = new HashMap<>();
						maps.put("time2", new JobParameter(System.currentTimeMillis()));
						JobParameters parameters = new JobParameters(maps);
						JobExecution jobExecution = jobLauncher.run(job, parameters);
						System.out.println("JobExecution: " + jobExecution.getStatus().toString());
						
						
						break;
					case (Event.MODIFY):
						//file.copyTo(dest, true);
					
						/*
						 * Map<String, JobParameter> maps1 = new HashMap<>(); maps1.put("time3", new
						 * JobParameter(System.currentTimeMillis())); JobParameters parameters1 = new
						 * JobParameters(maps1); JobExecution jobExecution1 = jobLauncher.run(job,
						 * parameters1); System.out.println("JobExecution: " +
						 * jobExecution1.getStatus().toString());
						 */
						
						break;
					case (Event.RENAME): {
						javaxt.io.File orgFile = new javaxt.io.File(event.getOriginalFile());
						dest = new javaxt.io.File(
								destination + orgFile.toString().substring(source.toString().length()));
						dest.rename(file.getName());
						
						break;

					}

					}
				}

			}

		}

	}

	@RequestMapping("/manual")
	public ModelAndView manualSchedule() throws Exception {
		ModelAndView modelAndView = new ModelAndView("home");
		List<String> sqlfiles = new ArrayList<>();
		File folder = new File("D:\\Teja\\SqlQueries");
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {

			sqlfiles.add(file.getName());
		}
		modelAndView.addObject("checkManual", "manual");
		modelAndView.addObject("file", sqlfiles);

		return modelAndView;
	}

	@RequestMapping("/manualmode")
	public ModelAndView manualmodeSch(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView modelAndView = new ModelAndView("home");
		
		String scheTime = request.getParameter("ScheTime");
		String[] fileNames = request.getParameterValues("sqlfile");
		

		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		res = new Resource[fileNames.length];
		
		int i=0;
			try {
				Resource[] resources = resolver.getResources("file:D:\\Teja\\SqlQueries/*.sql" );
				
				for (Resource resource : resources) {
					
					for (String resource2 : fileNames) {
						
						
						if(resource.getFilename().equalsIgnoreCase(resource2))
						{
							
							res[i] =resource;
							i++;
						}
					}
				}
			}
				catch (Exception e) {
					// TODO Auto-generated catch block
					
					e.printStackTrace();
					
				}
			TimerTask task = new TimerTask() {

				@Override
				public void run() {
			
				Map<String, JobParameter> maps = new HashMap<>();
				maps.put("time3", new JobParameter(System.currentTimeMillis()));

				SQLConfig config = new SQLConfig();
				config.setResources(res);
				JobParameters parameters = new JobParameters(maps);
				JobExecution jobExecution;
				try {
					jobExecution = jobLauncher.run(job2, parameters);
					System.out.println("JobExecution: " + jobExecution.getStatus().toString());
				} catch (JobExecutionAlreadyRunningException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JobRestartException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JobInstanceAlreadyCompleteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JobParametersInvalidException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				}
				};
				try {
					Date futureDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(scheTime);
					System.out.println(futureDate);
					Timer timer = new Timer();
					timer.schedule(task, futureDate);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		return modelAndView;
	}

	
	
}

