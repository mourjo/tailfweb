if ! [ -e lein ]
then
   wget -O lein https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
   chmod a+x lein
   lein
fi
