FROM python
EXPOSE 5000
WORKDIR /app
COPY app /app
RUN pip install -r requirements.txt
CMD ["flask", "run", "--host", "0.0.0.0", "--debug"]
