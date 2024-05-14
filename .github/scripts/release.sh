# back merging master to development
git checkout master
git pull origin master
cd sdk-logger
../gradlew publish --no-daemon --no-parallel