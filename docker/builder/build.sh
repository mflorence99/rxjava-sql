sed "s/__proxy__/$_PROXY/g" Dockerfile > Dockerfile.tmp
sed  -i "" "s/__proxyHost__/$_PROXY_HOST/g" Dockerfile.tmp
sed  -i "" "s/__proxyPort__/$_PROXY_PORT/g" Dockerfile.tmp
sed  -i "" "s/__proxyUser__/$_PROXY_USER/g" Dockerfile.tmp
sed  -i "" "s/__proxyPassword__/$_PROXY_PASSWORD/g" Dockerfile.tmp
docker build -t mflo999/rxjava-sql-builder -f Dockerfile.tmp .
