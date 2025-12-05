FROM python
EXPOSE 5000
WORKDIR /app
COPY app/requirements.txt /app
RUN pip install -r requirements.txt
COPY app /app
CMD ["flask", "run", "--host", "0.0.0.0", "--debug"]
