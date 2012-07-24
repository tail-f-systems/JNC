'''
Created on 24 jul 2012

@author: emil
'''

class MyClass(object):
    '''
    classdocs
    '''


    def __init__(self):
        '''
        Constructor
        '''
        self._l = []
    
    @property
    def l(self):
        return self._l
    
    @l.setter
    def l(self, value):
        print 'hi'
        self._l.append(value)

if __name__ == "__main__":
    C = MyClass()
    C.l = 1
    C.l = ' '
    C.l.append(2)
    print C.l