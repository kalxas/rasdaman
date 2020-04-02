import os
import sys
import signal
import errno

class Daemon(object):

	def __init__(self, pidfile, fd0 = os.devnull, fd1 = os.devnull, fd2 = os.devnull):
		self._status = False
		self._pidfile = pidfile
		self._fd0 = fd0 
		self._fd1 = fd1
		self._fd2 = fd2

	def __update_status(self):
		if os.path.isfile(self._pidfile):
			try:
				pid = self.get_pid()
				os.kill(pid, 0)
				self._status = True
			except OSError as e:
				if e.errno == errno.ESRCH:
					# if the pid doesn't exist, neither should the pidfile
					if os.path.isfile(self._pidfile):
						os.remove(self._pidfile)
					self._status = False
			except TypeError:
				self._status = False
		else:
			self._status = False


	def get_pid(self):
		try:
			pid = int(open(self._pidfile).read())
			return pid
		except IOError as e:
			sys.stderr.write("Failed to read pid file: %s\n" % str(e))

	def running(self):
		self.__update_status()
		return self._status


	def daemonize(self):
		try:
			pid = os.fork()
			#  parent process
			if (pid != 0):
				os._exit(0)
		except OSError as e:
			sys.stderr.write("Failed to create a child process: %s\n" % str(e))
			sys.exit(1)

		os.setsid()
		os.umask(0)


		# a second fork to ensure that the daemon is not a session leader
		try:
			pid = os.fork()
			#  parent process
			if (pid != 0):
				os._exit(0)
		except OSError as e:
			sys.stderr.write("Failed to create a child process: %s\n" % str(e))
			sys.exit(1)


		# PIDFILE
		pid = os.getpid()
		with open(self._pidfile, 'w') as pf:
			pf.write(str(pid))


		fd_0 = os.open(self._fd0, os.O_RDONLY | os.O_CREAT)
		fd_1 = os.open(self._fd1, os.O_RDWR | os.O_CREAT)
		fd_2 = os.open(self._fd2, os.O_RDWR | os.O_CREAT)


		os.dup2(fd_0, sys.stdin.fileno())
		os.dup2(fd_1, sys.stdout.fileno())
		os.dup2(fd_2, sys.stderr.fileno())


		self._status = True

	def start(self):

		if self.running():
			pid = self.get_pid()
			sys.stderr.write("Daemon with pid %s is already running, please stop it first\n" % str(pid))
			sys.exit(1)
		else:
			self.daemonize()
			self.run()

	def stop(self):
		# Make sure _status is updated
		self.__update_status()
		if self.running():
			pid = self.get_pid()
			try:
				os.kill(pid, signal.SIGTERM)
			except:
				sys.stderr.write("Could not kill process with pid: %s\n" % str(pid))
				sys.exit(1)
			os.remove(self._pidfile)

		else:
			sys.stderr.write("Daemon is not running\n")

		self._status = False


	def restart(self):
		if self.running():
			self.stop()
		if not self.running():
			self.start()

	def run(self):
		"""
		Method to be overridden
		"""