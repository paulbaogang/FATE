#
#  Copyright 2019 The FATE Authors. All Rights Reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
from arch.task_manager.db.models import JobInfo, DB
import datetime


def save_job_info(job_id, **kwargs):
    DB.create_tables([JobInfo])
    job = JobInfo()
    job.job_id = job_id
    job.create_date = datetime.datetime.now()
    for k, v in kwargs.items():
        setattr(job, k, v)
    job.save(force_insert=True)


def query_job_info(job_id):
    jobs = JobInfo.select().where(JobInfo.job_id == job_id)
    return [job.to_json() for job in jobs]


def update_job_info(job_id, update_data):
    query = JobInfo.update(**update_data).where(JobInfo.job_id == job_id)
    return query.execute()
