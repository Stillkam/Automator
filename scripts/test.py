#!/usr/bin/env python

a = [
"9c3d04a15d13575ba24e301ca748ad379ce9bc7e.apk",
"85865cd36efd0cc7b0f1caaadf014a5bdf62edb6.apk",
"6f2b9555d05413ef9c3709079f753ef0eda0408c.apk",
"01a07f9950711b175daac1a071838507a24fd3af.apk",
 "90c1055d3235102ce458eb80af0110c96ba15680.apk",
 "43a28e6558e851ed8af043dbc30930a10bc17037.apk",
 "d535246fdc9811513e734f9f23490203e2dae13e.apk",
 "1249bc3611bff64c11173e76630872dbf88880ed.apk",
 "59ced293035def2c66f058d6573859bfb2453981.apk",
 "7b7bafbb79360c8a9a236a1a550e2127074052ea.apk ",
]

import subprocess
for i in a:
    print subprocess.check_output(("./getPackageName.sh /home/winkar/Documents/github/LoginUI/apkCrawler/download/full/%s" % i).split())
