# BEGIN { @load "readfile" }
/<\/capabilities>/ {
    print "<notification>\n  <enabled>true</enabled>\n </notification>"
}
/<\/confdConfig>/ {
    print readfile("confd.conf.part")
}
{ print }
