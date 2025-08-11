import json
from datetime import datetime

from airflow.decorators import dag, task
from airflow.providers.http.operators.http import SimpleHttpOperator

@dag(
    dag_id="Active_Account",
    start_date=datetime(2024, 11, 19),
    schedule='*/40 * * * *',  # Every 10 seconds
    catchup=False
)
def dag1():
    @task
    def begin():
        http_task = SimpleHttpOperator(
            task_id='begin',
            http_conn_id='account_api',
            endpoint='/objects',
            method='GET',
            response_filter=lambda response: json.loads(response.text),
            do_xcom_push=True
        )
        return http_task.execute(context={})

    @task
    def sink(d):
        print(d)

    data = begin()
    sink(data)
dag1()
