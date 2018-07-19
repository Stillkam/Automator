#!/usr/bin//env ruby

def sh(*args)
    command = args.join(' ')
    # echo command
    `#{command}`
end

def aapt(*args)
    sh "aapt #{args.join(' ')}"
end

def getPackageName(apkPath)
    aapt("dump badging #{apkPath} | " \
        "grep  package | awk '{print $2}' | sed s/name=//g | sed s/\\'//g").chomp
end


apk_dir = "./apkCrawler/download/full/"
`ls #{apk_dir}`.split.each { |apk|
    `mv #{apk_dir + '/' + apk} #{apk_dir}/#{getPackageName apk_dir + '/' + apk}.apk`
}
