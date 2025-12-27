from crontab import CronTab

# Create a new cron job for the current user
def auto_cleaner_setup():
    cron = CronTab(user=True)
    command = "python3 /app/auto_cleaner/cleaner.py"
    # Check if job already exists to avoid duplicates
    for job in cron:
        if job.command == command:
            print("Cron job already exists")
            break
    else:
        job = cron.new(command=command)
        job.setall('0 2 1 * *')  # run at 2:00 am on 1st day of every month 

        cron.write()
        print("Cron job added")
