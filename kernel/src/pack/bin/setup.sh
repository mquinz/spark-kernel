#!/bin/bash
#set -x

SPARKKERNEL="$( cd "$( dirname "${BASH_SOURCE[0]}" )"/.. && pwd )"

SPARK_HOME=`echo 'import os; print os.environ["SPARK_HOME"]' | dse pyspark`

if [ `uname` == "Darwin" ]; then
  JUPYTER_CONFIGS=$HOME/Library/Jupyter/kernels
else
  JUPYTER_CONFIGS=$HOME/.local/share/jupyter/kernels
fi

if [ "$1" == "" ] ; then
  CLUSTER_HOSTNAME=127.0.0.1
else
  CLUSTER_HOSTNAME=$1
  MASTER=spark://$CLUSTER_HOSTNAME:7077
fi

if [ "$MASTER" == "" ]; then
   MASTER=spark://127.0.0.1:7077
fi

PY4J=$(ls $SPARK_HOME/python/lib/py4j*.zip)

SPARKLOCAL="$JUPYTER_CONFIGS/spark-dse-local"
SPARKCLUSTER="$JUPYTER_CONFIGS/spark-dse-cluster"
PYSPARKLOCAL="$JUPYTER_CONFIGS/pyspark-dse-local"
PYSPARKCLUSTER="$JUPYTER_CONFIGS/pyspark-dse-cluster"


## File resource doesn't work correctly here because it writes a byte-order mark on the file
## and python can't then parse it

mkdir -p $SPARKLOCAL

## File resource doesn't work correctly here because it writes a byte-order mark on the file
## and python can't then parse it

cat >$SPARKLOCAL/kernel.json <<EOF
{
    "display_name": "Spark-DSE Local (Scala 2.10.4)",
    "language": "scala",
    "argv": [
        "$SPARKKERNEL/bin/sparkkernel-dse",
        "--profile",
        "{connection_file}",
        "--spark-configuration",
        "spark.cassandra.connection.host=$CLUSTER_HOSTNAME"
     ],
     "codemirror_mode": "scala"
}
EOF

mkdir -p $SPARKCLUSTER

cat <<EOF >$SPARKCLUSTER/kernel.json
{
    "display_name": "Spark-DSE Cluster (Scala 2.10.4)",
    "language": "scala",
    "argv": [
        "$SPARKKERNEL/bin/sparkkernel-dse",
        "--profile",
        "{connection_file}",
        "--spark-configuration",
        "spark.cassandra.connection.host=$CLUSTER_HOSTNAME",
        "--spark-configuration",
        "spark.executor.memory=2g",
        "-master",
        "$MASTER"
     ],
     "codemirror_mode": "scala"
}
EOF

mkdir -p $PYSPARKLOCAL

cat <<EOF >$PYSPARKLOCAL/kernel.json
{
 "display_name": "Pyspark DSE Local",
 "env": { "MASTER":"local",
          "PYTHONPATH" : "$PY4J:$SPARK_HOME/python",
          "PYTHONSTARTUP":"$SPARK_HOME/python/pyspark/shell.py"},
 "argv": ["python", "-m", "IPython.kernel", "-f", "{connection_file}"],
 "language": "python"
}
EOF

mkdir -p $PYSPARKCLUSTER

cat <<EOF >$PYSPARKCLUSTER/kernel.json
{
 "display_name": "Pyspark DSE Cluster",
 "env": { "PYTHONPATH" : "$PY4J:$SPARK_HOME/python",
          "PYTHONSTARTUP":"$SPARK_HOME/python/pyspark/shell.py"},
 "argv": ["python", "-m", "IPython.kernel", "-f", "{connection_file}"],
 "language": "python"
}
EOF

