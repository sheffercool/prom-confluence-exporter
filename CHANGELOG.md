# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

[Tags on this repository](https://github.com/AndreyVMarkelov/prom-confluence-exporter/releases)

## [Unreleased]

## [1.0.20] (v5.4.1 - 7.2.x)
- Fix NPE if attachments empty for versions after 5.7.x
- upgrade prometheus client to 0.8.0

## [1.0.19] (v5.4.1 - 7.0.1)

- confluence_jmx_request_avg_exectime_of_ten_requests
- confluence_jmx_request_current_served_number
- confluence_jmx_request_errorpage_count
- confluence_jmx_request_num_in_last_ten_seconds

## [1.0.18] (v5.4.1 - 6.15.9)

- confluence_jmx_indexstat_flushing (Indicate whether the cache is currently flushing)
- confluence_jmx_indexstat_last_duration (Time taken during last indexing (ms))
- confluence_jmx_indexstat_task_queue (Shows number of tasks in the queue)
- confluence_jmx_indexstat_reindexing (Indicates whether Confluence is currently reindexing)
- confluence_jmx_systemstat_db_latency (Shows the latency of an example query performed against the database)
