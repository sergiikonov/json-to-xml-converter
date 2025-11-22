# 📊 JSON to XML Converter

**This console application was developed as part of the Block 1 assignment (Java Core). It is designed for parallel processing of large JSON files from a directory to calculate the frequency of values for a specified attribute. The program strictly adheres to requirements for efficient memory usage (JSON Streaming).**

## 🌟 Project Overview
**Goal:** Read all JSON files from a specified folder, calculate statistical frequencies for a given attribute, and write the sorted result to an XML file.

#### Core Data Model
| Entity (Many)  | Attributes | Relationship |
| ------------- |:-------------:|:-------------:|
| Movie      | _title_, _release_year_, _duration_minutes_, _genres_ (Multi-value)     | Many-to-One|
| Director      | _director_  | Belongs to Movie|

## 🛠️ Technology Stack
- Java: Core Logic & Concurrency (ExecutorService).
- Jackson Core (Stream API): Used for reading large JSON files chunk-by-chunk without loading the entire content into memory.
- JAXP (DOM): Standard Java API used for creating and writing the XML output.
- JUnit 5: Unit testing framework.

## ⚙️ How to Run the Application
The program requires two command-line arguments.

**Requirements**
- Java 17+

- The application must be bundled into a single executable JAR (including Jackson dependencies).

**Execution Command**
```
java -jar <your-app-name>.jar <path/to/json/folder> <attribute_name>
```
| Parameter |	Example Value |	Purpose |
| ------------- |:-------------:|:-------------:|
| <path/to/json/folder>	| ./data/films	 | Path to the directory containing *.json files. |
\<attribute_name>| genres	 | The attribute to be grouped and counted for statistics. |

## 📊 Supported StatisticsThe program supports aggregation based on the following attributes:
_director_ - The director's name. Single Value.

_genres_ - List of genres.	Multi-Value (Array Aggregation).

_release_year_ - The film's release year. Single Value.

## 📁 Examples (Input/Output)
### Sample Input (File: data_1.json)
```
[
  {
    "title": "Dune",
    "release_year": 2021,
    "director": "Villeneuve",
    "genres": ["Sci-Fi", "Drama"]
  },
  {
    "title": "Tenet",
    "release_year": 2020,
    "director": "Nolan",
    "genres": ["Action", "Sci-Fi"]
  }
]
```
### Expected Output
If the application is run with the argument genres:
```
java -jar app.jar ./data/films genres
```

#### Output File: statistic_by_genres.xml
```
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<statistics>
    <item>
        <value>Sci-Fi</value>
        <count>2</count>
    </item>
    <item>
        <value>Drama</value>
        <count>1</count>
    </item>
    <item>
        <value>Action</value>
        <count>1</count>
    </item>
</statistics>
```
_(Note: The file content is always sorted by the count in descending order.)_

To evaluate the efficiency of multithreaded processing, an experiment was conducted on a test set of **10 JSON files** 
(generated data), collecting statistics for the _director_ attribute.

| Number of Threads | 	Total Execution Time (ms) | 	Speedup (vs. 1 thread) |
|-------------------|:--------------------------:|:-----------------------:|
| 1 (Single Thread) |          	236 ms           |         	1.00x          |
| 2	                |          116 ms	           |          2.03x          |
| 4	                |           61 ms	           |          3.87x          |
| 8	                |           60 ms	           |          3.93x          |

## Analysis and Conclusion
### Conclusion:

The results clearly demonstrate that the application successfully utilizes multithreading for parallel processing of JSON files:

Near-Linear Speedup (1 → 4 threads): Doubling the thread count from 1 to 2 resulted in a near-perfect 2x speedup (236ms → 116ms). Increasing to 4 threads resulted in a 3.87x speedup, demonstrating excellent parallel scaling.

Performance Plateau (4 → 8 threads): Increasing the thread count further to 8 yielded almost no benefit (61ms vs 60ms).

Nature of the Task: This indicates that the task became IO-bound (limited by the speed of the disk reading the files) or CPU-saturated (the physical core count of the testing machine was likely 4 or less). The overhead of creating and managing 8 threads consumed the slight gains achieved beyond 4 threads.

The benchmark confirms that 4 threads is the optimal configuration for this specific environment.
