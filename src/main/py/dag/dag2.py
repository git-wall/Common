import datetime
from http import HTTPStatus
from airflow import DAG
from airflow.operators.python import PythonOperator, BranchPythonOperator
import requests
from datetime import datetime, timedelta  # Import datetime for start_date

uri = "https://api.thecatapi.com/v1/images/search?limit=10&breed_ids=beng&api_key="

def getApiKey(**kwargs):
    kwargs['ti'].xcom_push(key="token", value="REPLACE_ME")

def execute(**kwargs):
    token = kwargs['ti'].xcom_pull(key="token", task_ids="getApiKey")
    print("Token: " + token)
    
    result, status = handler(token)
    
    if status == False:
        raise Exception("Error")
    else:
        print("Success: " + result)
    
def handler(token):
    url = uri + token
    
    # headers = {
    #     "Authorization": "Bearer " + token,
    #     "Content-Type": "application/json"
    # }
    
    response = requests.get(url)
    
    if response.status_code == HTTPStatus.UNAUTHORIZED:
        response = requests.get(url)
    
    if response.status_code != HTTPStatus.OK:
        print(response.content)
        return response, False
    
    return response.json(), True

default_args = {
    'retries': 2,
    'retry_delay': timedelta(minutes=1),
    'owner': '188219-Thanh',
}

with DAG(
    dag_id="dag2",
    schedule_interval='@daily',
    start_date=datetime(2025, 1, 1),
    catchup=False,
    default_args=default_args,
    tags=["cat"]
) as dag:
    getApiKey = PythonOperator(
        task_id="getApiKey",
        python_callable=getApiKey
    )
    
    execute = BranchPythonOperator(
        task_id="catapi",
        python_callable=execute
    )
    
    getApiKey >> execute
