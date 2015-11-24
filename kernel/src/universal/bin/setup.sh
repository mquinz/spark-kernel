#!/bin/bash
#set -x

SPARKKERNEL="$( cd "$( dirname "${BASH_SOURCE[0]}" )"/.. && pwd )"

ASSEMBLYJAR=$(echo $SPARKKERNEL/lib/kernel-assembly-*.jar)

SPARK_HOME=`echo 'import os; print os.environ["SPARK_HOME"]' | dse pyspark`

if [ `uname` == "Darwin" ]; then
  JUPYTER_CONFIGS=$HOME/Library/Jupyter/kernels
else
  JUPYTER_CONFIGS=$HOME/.local/share/jupyter/kernels
fi

if [ -n "$1" ] ; then
  MASTER=spark://$1:7077
fi

if [ -z "$MASTER" ]; then
   MASTER=spark://127.0.0.1:7077
fi

PY4J=$(ls $SPARK_HOME/python/lib/py4j*.zip)

SPARKLOCAL="$JUPYTER_CONFIGS/spark-dse-local"
SPARKCLUSTER="$JUPYTER_CONFIGS/spark-dse-cluster"
PYSPARKLOCAL="$JUPYTER_CONFIGS/pyspark-dse-local"
PYSPARKCLUSTER="$JUPYTER_CONFIGS/pyspark-dse-cluster"


## File resource doesn't work correctly here because it writes a byte-order mark on the file
## and python can't then parse it


## File resource doesn't work correctly here because it writes a byte-order mark on the file
## and python can't then parse it

mkdir -p $SPARKLOCAL
echo "Creating file $SPARKLOCAL/kernel.json"
cat >$SPARKLOCAL/kernel.json <<EOF
{
    "display_name": "Spark-DSE Local",
    "language": "scala",
    "argv": [
        "dse",
        "spark-submit",
        "--master",
        "local[*]",
        "$ASSEMBLYJAR",
        "com.ibm.spark.SparkKernel",
        "--profile",
        "{connection_file}"
     ],
     "codemirror_mode": "scala"
}
EOF

mkdir -p $SPARKCLUSTER
echo "Creating file $SPARKCLUSTER/kernel.json"
cat <<EOF >$SPARKCLUSTER/kernel.json
{
    "display_name": "Spark-DSE Cluster",
    "language": "scala",
    "argv": [
        "dse",
        "spark-submit",
        "--master",
        "$MASTER",
        "$ASSEMBLYJAR",
        "com.ibm.spark.SparkKernel",
        "--profile",
        "{connection_file}"
     ],
     "codemirror_mode": "scala"
}
EOF

mkdir -p $PYSPARKLOCAL
echo "Creating file $PYSPARKLOCAL/kernel.json"
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
echo "Creating file $PYSPARKCLUSTER/kernel.json"
cat <<EOF >$PYSPARKCLUSTER/kernel.json
{
 "display_name": "Pyspark DSE Cluster",
 "env": { "PYTHONPATH" : "$PY4J:$SPARK_HOME/python",
          "PYTHONSTARTUP":"$SPARK_HOME/python/pyspark/shell.py"},
 "argv": ["python", "-m", "IPython.kernel", "-f", "{connection_file}"],
 "language": "python"
}
EOF

