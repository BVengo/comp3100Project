#!/bin/bash
# to kill multiple runaway processes, use 'pkill runaway_process_name'
# For the Java implementation, use the following format: ./test.sh Client.class -n <host> <port> <algorithm>
# For testing, using ./test.sh Client.class -n localhost 50000 lrr
dsSimDir="./ds-sim"
configDir="./configs"
outputDir="./tests"
buildDir="./build"

if [[ ! -d $dsSimDir ]]; then
	echo "No $dsSimDir found!"
	exit
fi

if [[ ! -d $configDir ]]; then
	echo "No $configDir found!"
	exit
fi

if [[ ! -d $outputDir ]]; then
	echo "No $outputDir found!"
	exit
fi

if [[ $# -lt 1 ]]; then
	echo "Usage: $0 Client.class -n <host> <port> <algorithm>"
	echo "Valid algorithms are currently lrr or fc."
	exit
fi

if [[ $@ != *".class"* ]]; then
	echo "No class file or .class extension missing!"
	exit
fi

#if [ ! -f $1 ]; then
	#echo "No $1 found!"
	#echo "Usage: $0 your_client [user-specified argument...]"
	#exit
#fi

trap "kill 0" EXIT

newline=""

args=$@
for arg in $@; do
	if [[ $arg == "-n" ]]; then
		args=$(sed 's/-n//' <<< $@)
		newline="n"
		break
	fi
done

cArgIndex=0
for arg in $args; do
	if [[ $cArgIndex == 3 ]]; then
		algorithm="$arg"
		echo "Algorithm $arg selected."
	fi

	if [[ $cArgIndex > 0 ]]; then
		clientArgs+=" $arg"
		(( cArgIndex+=1 ))
	fi

	if [[ $arg == *".class" ]]; then
		yourClient="$arg"
		cArgIndex=$((1))
	fi
done

if [[ ! -f $buildDir/$yourClient ]]; then
	echo "No $1 found!"
fi


if [[ ! -d $outputDir/$algorithm ]]; then
	echo "Making tests algorithm folder $outputDir/$algorithm."
	mkdir $outputDir/$algorithm
	exit
fi


diffLog="$algorithm.diff"
if [[ -f $outputDir/$algorithm/$diffLog ]]; then
	rm $outputDir/$algorithm/*-log.txt
	rm $outputDir/$algorithm/log-diff.txt
	rm $outputDir/$algorithm/$diffLog
fi

for conf in $configDir/*.xml; do
	echo "$conf"
	echo ----------------
	echo "running the reference implementation (./ds-client)..."
	sleep 1
	
	outName="$outputDir/$algorithm/${conf##*/}"

	if [[ $newline == "n" ]]; then
		$dsSimDir/ds-server -c $conf -v brief -n > $outName-ref-log.txt&
		sleep 4
		$dsSimDir/ds-client -a $algorithm -n
	else
		$dsSimDir/ds-server -c $conf -v brief > $outName-ref-log.txt&
		sleep 4
		$dsSimDir/ds-client -a $algorithm
	fi
	
	echo "running your implementation ($yourClient)..."
	sleep 2
	if [[ $newline == "n" ]]; then
		$dsSimDir/ds-server -c $conf -v brief -n > $outName-my-log.txt&
	else
		$dsSimDir/ds-server -c $conf -v brief > $outName-my-log.txtg&
	fi
	sleep 4
	java -cp $buildDir $(sed 's/\.class//' <<< $yourClient)$clientArgs
	
	sleep 1
	diff $outName-ref-log.txt $outName-my-log.txt > $outputDir/$algorithm/log-diff.txt
	if [[ -s $outputDir/$algorithm/log-diff.txt ]]; then
		echo NOT PASSED!
	elif [ `wc -c < $outName-ref-log.txt` -eq 0 -a `wc -c < $outName-my-log.txt` -eq 0 ]; then
		echo "NOT PASSED (no log files)!"
	else
		echo PASSED!
	fi
	echo ============
	sleep 1
	cat $outputDir/$algorithm/log-diff.txt >> $outputDir/$algorithm/$diffLog
done

rm ds-jobs.xml
rm ds-system.xml

echo "testing done! check $outputDir/$algorithm/$diffLog"

