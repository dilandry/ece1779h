			long twentyFourHrs = 1000 * 60 * 60 * 24;
        	int oneHour = 60 * 60;
        	int oneMinute = 60;
        	int fiveMinutes = 5*60;
			
			// Granularity is 1 or 5 minutes, depending on "Detailed Monitoring" option.
			GetMetricStatisticsRequest cpuRequest = new GetMetricStatisticsRequest()
			            									.withStartTime(new Date(new Date().getTime()- twentyFourHrs))
            												.withNamespace("AWS/EC2")
            												.withPeriod(fiveMinutes)
															.withMetricName("CPUUtilization")
															.withStatistics("Average", "Maximum")
															.withEndTime(new Date());
        	
			GetMetricStatisticsResult cpuResult = cw.getMetricStatistics(cpuRequest);
			
			for (Datapoint cpuPoint : cpuResult.getDatapoints()){
				out.print(cpuPoint.getMaximum() + "/" +
						  cpuPoint.getAverage() + " ,");
			}