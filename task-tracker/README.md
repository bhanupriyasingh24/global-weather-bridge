# Task Tracker

A simple task management application using Java, plain HTML/CSS, and SQLite.

## Running Locally

```bash
./run_server.sh
```
Access at http://localhost:8080.

## Cloud Run Deployment Notes

This application uses SQLite (`task_database.db`) which stores data in a local file.
**Warning**: Google Cloud Run containers are stateless. The database file will be reset whenever the container restarts or a new revision is deployed. The tasks will NOT persist permanently in the cloud version unless you configure a persistent volume or switch to Cloud SQL.
