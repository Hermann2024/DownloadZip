# lancement de batch par URL
# compatible wget 1.13

if [ $# -ne 1 ]
then
#	echo "Usage: launchBatch.sh [BATCH_NAME] [ID_BDF] [FILE_NAME]"
	echo "Usage: launchBatch.sh [BATCH_NAME]"
	exit 1
fi

BATCH_NAME=$1
#ID_BDF=$2
#FILE_NAME=$3
UR_URL="http://localhost:8080/webur"
LOGIN="admin" 
PASS="password"
#Repertoire de traitement
LAUNCH_DIR="/data/cylande/launchbatch"
#fichier de retour du lancement du batch pour exploitation des resultats
RESULT_FILE="result.xml"
COOKIE_FILE="cookies.txt"

cd $LAUNCH_DIR

#Repertoire de traitement
TRT_DIR=$LAUNCH_DIR/$$
mkdir $TRT_DIR
cd $TRT_DIR

wget -o out --save-cookies=$COOKIE_FILE --keep-session-cookies "$UR_URL/WelcomePortal"

#lancement de l'authentification 
wget -o out --load-cookies=$COOKIE_FILE --save-cookies=$COOKIE_FILE --keep-session-cookies --post-data="j_username=$LOGIN&j_password=$PASS" "$UR_URL/j_security_check"

#lancement du bach
#wget -a out -O $RESULT_FILE --load-cookies $COOKIE_FILE --keep-session-cookies  "$UR_URL/batchEngine?action=execute&batchName=$BATCH_NAME&idbdf=$ID_BDF&filename=$FILE_NAME&domain=default"
wget -a out -O $RESULT_FILE --load-cookies=$COOKIE_FILE --keep-session-cookies "$UR_URL/batchEngine?action=execute&batchName=$BATCH_NAME&domain=default"

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
