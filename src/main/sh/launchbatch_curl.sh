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

curl "$UR_URL/WelcomePortal"  --cookie "$COOKIE_FILE" --cookie-jar "$COOKIE_FILE" --location

#lancement de l'authentification 
curl "$UR_URL/j_security_check"  --cookie "$COOKIE_FILE" --cookie-jar "$COOKIE_FILE" -d "j_username=$LOGIN&j_password=$PASS"

#lancement du bach
curl "$UR_URL/batchEngine?action=execute&batchName=$BATCH_NAME" --output $RESULT_FILE --cookie "$COOKIE_FILE" --cookie-jar "$COOKIE_FILE"


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
