# back merging master to development
git checkout master
git pull origin master
./gradlew assemble --stacktrace
cd sdk-logger
../gradlew publish --no-daemon --no-parallel