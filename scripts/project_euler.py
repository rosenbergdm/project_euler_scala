#!/usr/bin/env python 

import os
import sys
import re
import lxml.html
import urllib2
import getopt


class EulerProblem(object):

  def __init__(self, prob_no, target_dir=None):
    self.problem_no = prob_no
    self.url = "http://projecteuler.net/index.php?section=problems&id=" + str(prob_no)
    self.problem_dir = ""
    if target_dir:
      self.problem_dir = os.path.join(os.path.curdir, target_dir)
    else:
      self.problem_dir = os.path.join(os.path.curdir, "problem" + str(prob_no))
    self.media = []
    self.text  = []
    self.html  = []
    self.raw   = []
    self.math  = []

  def setup_dir(self, flags=None):
    if os.path.exists(self.problem_dir):
      if flags and flags.__contains__("force"):
        os.sys.stderr.write(
            "Warning, directory already exists; proceeding due to force option.")
      else:
        os.sys.stderr.write("Error, directory already exists")
        sys.exit(3)
    else:
      os.mkdir(self.problem_dir)
    # TODO: copy in Makefile and problem template
    return

  def parse_problem(self, flags=None):
    self.html = lxml.html.parse(self.url).getroot()
    text = self.html.get_element_by_id("main_page").text_content().splitlines()
    empty_matcher = re.compile('^ *$')
    self.text = '\n'.join(filter(lambda x: empty_matcher.match(x) is None, text))
    self.get_media(flags)

  def get_media(self, flags=None):
    images = self.html.xpath("//div/img")
    if images.__len__() > 1:
      self.media = [ "http://projecteuler.net/" + i.attrib['src'] for i in images[1:] ]
      for image_url in self.media:
        fname = os.path.join(self.problem_dir, urllib2.urlparse.urlparse(image_url).path.split('/')[-1])
        outfile = open(fname, 'w')
        image_reader = urllib2.urlopen(image_url)
        outfile.write(image_reader.read())
        outfile.close()

  def math_to_tex(self, flags=None):
    pass

  def write_output(self, flags=None):
    pass

      
class Usage(Exception):

  def __init__(self, msg):
    self.msg = msg


def main(argv=None):
  if argv is None:
    argv = sys.argv
  try:
    try:
      opts, args = getopt.getopt(argv[1:], "hp:v", ["help", "output="])
    except getopt.error, msg:
      raise Usage(msg)
    
    problem_requested = None
  # option processing
    for option, value in opts:
      if option == "-v":
        verbose = True
      if option in ("-h", "--help"):
        raise Usage("help_message")
      if option in ("-p", "--problem"):
        problem_requested = value

    euler_prob = EulerProblem(problem_requested)
    euler_prob.setup_dir()
    euler_prob.parse_problem()
    # euler_prob.write_output()

  except Usage, err:
    print >> sys.stderr, sys.argv[0].split("/")[-1] + ": " + str(err.msg)
    print >> sys.stderr, "\t for help use --help"
    return 2


if __name__ == "__main__":
  sys.exit(main())
