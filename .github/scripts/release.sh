echo $OSS_PASSWORD
echo $OSS_STAGING_PROFILE_ID
echo $OSS_SIGNING_KEY_ID
echo $OSS_SIGNING_PASSWORD
echo $OSS_SIGNING_KEY
if [ -z $TEST ]
then
      echo "Test is null"
fi
if [ -z $OSS_SIGNING_KEY ]
then
      echo "Signing Key is null"
else
      echo "Signing Key Non null"
fi
./gradlew assemble