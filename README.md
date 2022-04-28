# Client for the DS-Sim Project
SID: 45898405 \
Name: Benjamin van de Vorstenbosch

---

This client is designed to connect to the ds-sim server from the [ds-sim repository](https://github.com/distsys-MQ/ds-sim) as a solution to the COMP3100 unit assignment.

**Directories**
- `build`: compiled java class files
- `configs`: server configuration XML files for testing
- `ds-sim`: precompiled DS-Sim files
- `src`: project source files
- `tests`: test log outputs

To compile from the root folder into the `build` folder, use the command 
```
./compile.sh
```
To run all tests from the root folder (after compilation), use the command 
```
./tests.sh Client.class -n localhost 50000 <algorithm>
```
for example
```
./tests.sh Client.class -n localhost 50000 lrr
```
