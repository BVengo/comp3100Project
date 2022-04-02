# Client for the DS-Sim Project
SID: 45898405 \
Name: Benjamin van de Vorstenbosch

---

This client is designed to connect to the ds-sim server from the [ds-sim repository](https://github.com/distsys-MQ/ds-sim) as a solution to the COMP3100 unit assignment.

For convenience, the pre-compiled ds-sim and ds-client files can be found in the `tests` folder.

To compile from the `src` folder into the `tests` folder, use the command 
```
javac -d ../tests *.java
```
The main class is 'Client'. Therefore, to run all tests from the `tests` folder (after compilation), use the command 
```
./S1Tests-wk6.sh Client.class -n localhost 50000 lrr
```
