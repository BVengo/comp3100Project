# Baseline Algorithms
## First-Fit (FF)
Allocates job by giving it to the first readily-available (inactive or active with no running jobs) server with enough resources.

- Fast execution time since jobs are given to the first server available
- Large memory usage since the job is allocated without seeing if there is a server with less resources available

## Best-Fit (BF)
Allocates a job to a readily-available server with the closest fitting resources (lowest cores in ds-sim).

- Low memory usage
- Higher execution time since all servers need to be checked for their capacity

## Worst-Fit (WF)
Allocates a job to the server with the largest set of resources (number of cores in ds-sim)

- Large internal fragmentation leaves room for smaller jobs to be placed in the remaining resources
- Higher execution time since all servers need to be checked for their capacity