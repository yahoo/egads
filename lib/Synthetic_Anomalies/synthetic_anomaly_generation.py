import random
import datetime as dt
import numpy as np
import matplotlib.pyplot as plt
import sys


# ----------------------- real time-series as input -----------------------
time, value = np.loadtxt("ts.txt", dtype = float,delimiter=",", unpack=True)

# -----------------read configurations for synthetic time-series ----------------
param = dict(line.strip().replace(" ", "").split('=') for line in open('config.properties') if not line.startswith('#') and not line.startswith('\n')) 


# ------------------- Time-series processing ( Mean and Std. deviation ) -------------------
day_hours = 24
week_hours = day_hours*7

if len(value) < 168:
	print "\n\t~--~  very less data! please input atleast a week of hourly data (i.e. day_hoursx7 datapoints).  ~--~\n"
	sys.exit(1)

daily = np.zeros((day_hours,2))
weekly = np.zeros((week_hours,2))
i = 0
for item in value:
	daily[i%day_hours][0] += item
	daily[i%day_hours][1] += 1
	weekly[i%(week_hours)][0] += item
	weekly[i%(week_hours)][1] += 1
	i = i + 1


daily_mean = np.array(daily[:,0]/daily[:,1])
weekly_mean = np.array(weekly[:,0]/weekly[:,1])

daily_var = np.zeros((day_hours,2))
weekly_var = np.zeros((week_hours,2))

i = 0
for item in value:
	daily_var[i%day_hours][0] += (item - daily_mean[i%day_hours]) * (item - daily_mean[i%day_hours])
	daily_var[i%day_hours][1] += 1
	weekly_var[i%(week_hours)][0] += (item - weekly_mean[i%(week_hours)]) * (item - weekly_mean[i%(week_hours)])
	weekly_var[i%(week_hours)][1] += 1
	i = i + 1

daily_std = np.sqrt(daily_var[:,0]/daily_var[:,1])
weekly_std = np.sqrt(weekly_var[:,0]/weekly_var[:,1])

#------------ OUTPUT_FORMAT ------------------

out_format = param['OUTPUT_FORMAT']
try:
	out_format = int(out_format)
	if(out_format !=0 and out_format !=1):
		raise ValueError('Invalid OUTPUT_FORMAT!')
except ValueError:
	print("Invalid OUTPUT_FORMAT!")
	sys.exit(1) 

#------------S_TYPE ------------------

ts_type = param['S_TYPE']
try:
	ts_choice = int(ts_type)
	if(ts_choice !=0 and ts_choice !=1):
		raise ValueError('Invalid S_TYPE!')
except ValueError:
	print("Invalid S_TYPE!")
	sys.exit(1) 

#------------NUM_WEEKS-----------------

temp_weeks = param['NUM_WEEKS']
try:
	temp_weeks = int(temp_weeks)
	if(temp_weeks > 52 or temp_weeks == 0):
		raise ValueError('Invalid NUM_WEEKS! should be <= 52 and > 0')
except ValueError:
	print("Invalid NUM_WEEKS! should be <= 52 and > 0")
	sys.exit(1)

num_of_weeks = temp_weeks

#-------------A_TYPE--------------------

anom_type = param['A_TYPE']
try:
	anom_choice = int(anom_type)
	if(anom_choice !=0 and anom_choice !=1 and anom_choice !=2):
		raise ValueError('Invalid A_TYPE!')
except ValueError:
	print("Invalid A_TYPE!")
	sys.exit(1)

#-------------FREQUENCY------------------

temp_freq = param['FREQUENCY']
try:
	freq = float(temp_freq)
	if(freq > 1.0 or freq < 0.0):
		raise ValueError('Invalid FREQUENCY!')
except ValueError:
	print("Invalid FREQUENCY!")
	sys.exit(1)

#-------------SEVERITY------------------

temp_sever = param['SEVERITY']
try:
	sever = float(temp_sever)
	if(sever > 1.0 or sever < 0.0):
		raise ValueError('Invalid SEVERITY!')
except ValueError:
	print("Invalid SEVERITY!")
	sys.exit(1)

weekly_avg_std = np.sum(weekly_std)*100/np.sum(weekly_mean)
daily_avg_std = np.sum(daily_std)*100/np.sum(daily_mean)

print "\n\t---~ Stats about actual time series ~---\n "
print ">> weekly avg (per hour) deviation of data in % : ",weekly_avg_std
print ">> daily avg (per hour) deviation of data in % : ",daily_avg_std,"\n"

#print weekly_std*100/weekly_mean
if out_format == 1:
	syn_ts = np.zeros(week_hours*num_of_weeks)
else:
	if week_hours*num_of_weeks <= len(value):
		syn_ts = np.zeros(week_hours*num_of_weeks)
	else:
		syn_ts = np.zeros(len(value))

if out_format == 1:
# ----------------- Generate new synthetic time-series(using input) ------------------
	# --------------------- Anomaly type 0 ------------------------------

		# --------------- Use of severity to calculate the % portion of std. deviation as anomaly ------------------
	s_mult = (sever*2*100)/((daily_avg_std+weekly_avg_std)/2)
	#print s_mult

	if anom_choice == 0:
		# -------- use of frequency ------------
		prob = np.random.choice([0,1],len(syn_ts),p=[1.0-freq,freq])

		if ts_choice == 0:
			for i in range(len(syn_ts)):
				if np.random.choice([0,1],1,p=[0.5,0.5])[0]:
					#--------- use of severity ------------
					syn_ts[i] = daily_mean[i%day_hours] + prob[i]*daily_std[i%day_hours]*s_mult
				else:
					syn_ts[i] = daily_mean[i%day_hours] - prob[i]*daily_std[i%day_hours]*s_mult
		else:
			for i in range(len(syn_ts)):
				if np.random.choice([0,1],1,p=[0.5,0.5])[0]:
					#--------- use of severity ------------
					syn_ts[i] = weekly_mean[i%(week_hours)] + prob[i]*weekly_std[i%(week_hours)]*s_mult
				else:
					syn_ts[i] = weekly_mean[i%(week_hours)] - prob[i]*weekly_std[i%(week_hours)]*s_mult


	# ---------------------- Anomaly type 1 -------------------------------

		# --------------- use of frequency ----------------
	start_pnt = random.randint(int(len(syn_ts)/2),int(len(syn_ts)-len(syn_ts)*(freq/2)))
	end_pnt = start_pnt + int(len(syn_ts)*(freq/2))

	if end_pnt > len(syn_ts)-1:
		end_pnt = len(syn_ts)-1


		# ----- gradual increase if 1 else gradual decrease in values --------
	inc_or_dec = np.random.choice([0,1],1,p=[0.5,0.5])[0]
	if inc_or_dec == 0 and sever >0.4:
		inc_or_dec = 1

	if anom_choice == 1:
		if ts_choice == 0:
			for i in range(len(syn_ts)):
				if(i>=start_pnt and i <= end_pnt):
					#--------- use of severity ------------
					if inc_or_dec:
						syn_ts[i] = daily_mean[i%day_hours] + daily_std[i%day_hours]*s_mult*(1.0/(end_pnt-start_pnt+1.0))*(i-start_pnt+1.0)
					else:
						syn_ts[i] = daily_mean[i%day_hours] - daily_std[i%day_hours]*s_mult*(1.0/(end_pnt-start_pnt+1.0))*(i-start_pnt+1.0)
				else:
					syn_ts[i] = daily_mean[i%day_hours]
		else:
			for i in range(len(syn_ts)):
				if(i>=start_pnt and i <= end_pnt):
					#--------- use of severity ------------
					if inc_or_dec:
						syn_ts[i] = weekly_mean[i%(week_hours)] + weekly_std[i%(week_hours)]*s_mult*(1.0/(end_pnt-start_pnt+1.0))*(i-start_pnt+1.0)
					else:
						syn_ts[i] = weekly_mean[i%(week_hours)] - weekly_std[i%(week_hours)]*s_mult*(1.0/(end_pnt-start_pnt+1.0))*(i-start_pnt+1.0)
				else:
					syn_ts[i] = weekly_mean[i%(week_hours)]


	# ------------------------- Anomaly type 2 ------------------------

		# on_off: behaviour flag
		# on_off = 1, random behaviour
		# on_off = 0, time-series shutoff
	on_off = np.random.choice([0,1],1,p=[0.5,0.5])[0]

	min_val = np.min(weekly_mean)
	max_val = np.max(weekly_mean)

	if anom_choice == 2:
		for i in range(len(syn_ts)):
			if(i>=start_pnt and i <= end_pnt):
				if on_off:
					syn_ts[i] = random.randint(int(min_val),int(max_val)) 
				else:
					syn_ts[i] = random.randint(int(min_val),int(min_val + (max_val - min_val)/100))
			else:
				if(ts_choice == 0):
					syn_ts[i] = daily_mean[i%day_hours]
				else:
					syn_ts[i] = weekly_mean[i%(week_hours)]

else:
# ------------------- Using real(input) time-series -----------------	
	# --------------------- Anomaly type 0 ------------------------------	

	if anom_choice == 0:
		# -------- use of frequency ------------
		prob = np.random.choice([0,1],len(syn_ts),p=[1.0-freq,freq])
		
		for i in range(len(syn_ts)):
			if np.random.choice([0,1],1,p=[0.5,0.5])[0]:
				#--------- use of severity ------------
				syn_ts[i] = value[i] + prob[i]*value[i]*sever*2
			else:
				syn_ts[i] = value[i] - prob[i]*value[i]*sever*2
	


	# ---------------------- Anomaly type 1 -------------------------------

		# --------------- use of frequency ----------------
	start_pnt = random.randint(int(len(syn_ts)/2),int(len(syn_ts)-len(syn_ts)*(freq/2)))
	end_pnt = start_pnt + int(len(syn_ts)*(freq/2))

	if end_pnt > len(syn_ts)-1:
		end_pnt = len(syn_ts)-1
	print start_pnt,end_pnt

		# ----- gradual increase if 1 else gradual decrease in values --------
	inc_or_dec = np.random.choice([0,1],1,p=[0.5,0.5])[0]
	if inc_or_dec == 0 and sever > 1:
		inc_or_dec = 1

	if anom_choice == 1:
		
		for i in range(len(syn_ts)):
			if(i>=start_pnt and i <= end_pnt):
				#--------- use of severity ------------
				if inc_or_dec:
					syn_ts[i] = value[i] + value[i]*sever*2*(1.0/(end_pnt-start_pnt+1.0))*(i-start_pnt+1.0)
				else:
					syn_ts[i] = value[i] - value[i]*sever*2*(1.0/(end_pnt-start_pnt+1.0))*(i-start_pnt+1.0)
			else:
				syn_ts[i] = value[i]


	# ------------------------- Anomaly type 2 ------------------------

		# on_off: behaviour flag
		# on_off = 1, random behaviour
		# on_off = 0, time-series shutoff
	on_off = np.random.choice([0,1],1,p=[0.5,0.5])[0]

	min_val = np.min(value)
	max_val = np.max(value)

	if anom_choice == 2:
		for i in range(len(syn_ts)):
			if(i>=start_pnt and i <= end_pnt):
				if on_off:
					syn_ts[i] = value[random.sample(range(0,start_pnt),1)[0]]
				else:
					syn_ts[i] = random.randint(int(min_val),int(min_val + (max_val - min_val)/100))
			else:
				syn_ts[i] = value[i]

# ------------------- plot --------------------

#red_points = np.argwhere(prob == 1).flatten()
dates=np.array([dt.datetime.fromtimestamp(long(ts)) for ts in time])



plt.figure(1)
plt.subplot(211)
plt.plot(dates[:len(syn_ts) if len(syn_ts)<=len(value) else len(value)],value[:len(syn_ts) if len(syn_ts)<=len(value) else len(value)])

plt.subplot(212)
plt.plot(dates[:len(syn_ts)],syn_ts)
#plt.plot(dates[red_points],syn_ts[red_points],'o',color='r')
plt.show()


