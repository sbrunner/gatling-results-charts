#!/usr/bin/python

import requests
from json import loads
from sys import argv

session = requests.Session()
session.auth = (argv[1], argv[2])

if len(argv) == 6:
    date_from = "%s_%s" % (argv[4], argv[3])
    date_until = "%s_%s" % (argv[5], argv[3])
elif len(argv) == 5:
    date_from = argv[3]
    date_until = argv[4]
else:
    exit("wrong number of arguments")

print("From: %s, until: %s" % (date_from, date_until))
response = session.get(
    "https://metrics.camptocamp.com/render?" + "&".join(["%s=%s" % item for item in [
        ("target", "nPercentile(avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.load.*.shortterm)),50)"),
        ("target", "nPercentile(avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.load.*.shortterm)),95)"),
        ("target", "nPercentile(avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.cpu.percent-idle)),50)"),
        ("target", "nPercentile(avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.cpu.percent-idle)),5)"),
        ("target", "nPercentile(avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.memory.memory-used)),50)"),
        ("target", "nPercentile(avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.memory.memory-used)),95)"),
        ("target", "nPercentile(avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.disk-vda.disk_ops.*)),50)"),
        ("target", "nPercentile(avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.disk-vda.disk_ops.*)),95)"),
        ("from", date_from),
        ("until", date_until),
        ("format", "json")
    ]]), verify=False
).text
try:
    response = loads(response)
    result = [r["datapoints"][0][0] for r in response]
    print("load:\t50: %.1f,\t95: %.1f" % (result[0], result[1]))
    print("CPU:\t50: %.0f, \t95: %.0f" % (100-result[2], 100-result[3]))
    print("memory:\t50: %.2f,\t95: %.2f" % (result[4]/1024/1024/1024, result[5]/1024/1024/1024))
    print("disk:\t50: %.1f,\t95: %.1f" % (result[6], result[7]))
except:
    print "Error:"
    print response
