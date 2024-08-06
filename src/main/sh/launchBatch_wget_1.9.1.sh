# lancement de batch par URL
# compatible wget 1.9.1

if [ $# -ne 3 ]
then
	echo "Usage: launchBatch.sh [BATCH_NAME] [ID_BDF] [FILE_NAME]"
	exit 1
fi

BATCH_NAME=$1
ID_BDF=$2
FILE_NAME=$3
UR_URL="http://localhost:8001/urmo"
LOGIN="cylande" 
PASS="cylande59"
#Repertoire de traitement
LAUNCH_DIR="/home/weblogic/testLaunchBatch"
#fichier de retour du lancement du batch pour exploitation des resultats
RESULT_FILE="result.xml"

cd $LAUNCH_DIR

#Repertoire de traitement
TRT_DIR=$LAUNCH_DIR/$$
mkdir $TRT_DIR
cd $TRT_DIR

#lancement de l'authentification 
wget -o out --post-data="j_username=$LOGIN&j_password=$PASS" "$UR_URL/j_security_check"
JSESSION=`grep -i jsession out | tail -n1 | sed -e "s/^.*jsessionid=\(.*\)!.*$/\1/"`

#lancement du bach
wget -a out -O $RESULT_FILE --header "Cookie: JSESSIONID=$JSESSION" "$UR_URL/batchEngine?action=execute&batchName=$BATCH_NAME&idbdf=$ID_BDF&filename=$FILE_NAME&domain=default"

#extraction du batchId
BATCH_ID=`grep "<id>" $RESULT_FILE |  sed -e "s/<id>\([0-9]*\)<\/id>/\1/"`

cd $LAUNCH_DIR

#Traitement des résultats
if [ "test$BATCH_ID" != "test" ]
then
  #lancement OK suite a attribution du batchId
  rm -fr $TRT_DIR
else
  #lancement KO 
  NOW=$(date +"%m%d%Y_%H%M%s")
  #archivage du repertoire temporaire
  tar cf error_$$_$NOW.tar $TRT_DIR/*
  rm -fr $TRT_DIR
  exit 1	
fi
