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

To run the ds-sim server, use the command (from the main folder)
```
./ds-sim/ds-server -v brief -c ./configs/<config> -n
```

To run the client manually (after compilation), use the command
```
java -cp ./build Client localhost 50000 <algorithm>
```


To run all tests from the root folder (after compilation), use the command 
```
./tests.sh Client.class -n localhost 50000 <algorithm>
```
for example
```
./tests.sh Client.class -n localhost 50000 lrr
```

The directories of files can be adjusted in test.sh.

To specifically run the Stage2 tests for turnaround time (after compilation), use the command
```
cd ./configs && ./stage2-test-x86 "java -cp ../build Client localhost 50000 ct" -o tt -n & cd ..
```
Unfortunately the stage2-test-x86 hardcoded the location of the config files, so it cannot be run from the parent directory. It also means there are now copies of ds-server littered throughout this project.