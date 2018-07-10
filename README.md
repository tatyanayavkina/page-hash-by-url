# page-hash-by-url

Reads url list, load content and calculate hash for each url. Result file is `result.txt`.
If app gets exception during hash calculating then error message are written to file instead of hash.

# How to run
`sbt "runMain com.bitbucket.tatianayavkina.PageHashByUrlApp <PATH_TO_FILE_WITH_URLS>"`