import sys
import pytest
import pexpect


class JncTestBase:
    def __init__(self, basedir):
        self.px_args = dict(cwd=basedir, encoding="utf-8", logfile=sys.stdout)
        self.basedir = basedir

    def start_confd(self):
        print('compile and start confd')
        confd = pexpect.spawn('make clean all start', **self.px_args)
        confd.expect(pexpect.EOF)
        confd.close()
        assert 0 == confd.exitstatus

    def start_jnc(self):
        print('compile and start the test')
        self.jnc_runner = pexpect.spawn('gradle clean run', **self.px_args)

    def cleanup(self):
        self.jnc_runner.close()
        confd = pexpect.spawn('make stop', **self.px_args)
        confd.expect(pexpect.EOF)
        confd.close()


@pytest.fixture(scope='function',
                params=['1-test-union',
                        '2-test-enumeration',
                        '3-test-decimal64',
                        '4-test-leafref',
                        '5-test-bits',
                        '6-test-binary',
                        '7-test-uint64',
                        '11-test-notification',
                        '12-test-augment'])
def jnc_test(request):
    jnc_test = JncTestBase(request.param)
    jnc_test.start_confd()
    jnc_test.start_jnc()
    yield jnc_test
    jnc_test.cleanup()


def run_notifier(jnc_test):
    notifier = pexpect.spawn('make start_notifier', **jnc_test.px_args)
    notifier.expect('notifier started')
    jnc_test.jnc_runner.expect('Waiting for "interface" notification')
    notifier.sendline('i')
    jnc_test.jnc_runner.expect('notification')
    jnc_test.jnc_runner.expect('link-up')
    jnc_test.jnc_runner.expect('Waiting for more "interface" notifications')
    notifier.sendline('m')
    for i in range(10):
        jnc_test.jnc_runner.expect_exact('notification')
        jnc_test.jnc_runner.expect_exact(['link-up', 'link-down'])
        jnc_test.jnc_runner.expect_exact('notification')
    notifier.close(force=True)


def test_run_jnc(jnc_test):
    if jnc_test.basedir == '11-test-notification':
        run_notifier(jnc_test)
    jnc_test.jnc_runner.expect('BUILD SUCCESSFUL in')
    jnc_test.jnc_runner.expect(pexpect.EOF)
    jnc_test.jnc_runner.close()
    assert 0 == jnc_test.jnc_runner.exitstatus
