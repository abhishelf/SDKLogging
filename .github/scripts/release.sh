# back merging master to development
git checkout master
git pull origin master
./gradlew assemble --stacktrace
cd sdk-logger
echo $OSS_PASSWORD
echo $OSS_STAGING_PROFILE_ID
echo $OSS_SIGNING_KEY_ID
echo $OSS_SIGNING_PASSWORD
echo $OSS_SIGNING_KEY
echo $TEST
echo TEST_WITHOUT_VAR
../gradlew publish --no-daemon --no-parallel
