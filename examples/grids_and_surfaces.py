from pygellan.acquire import PygellanBridge
import numpy as np
import matplotlib.pyplot as plt

#### Setup ####
#establish communication with Magellan
bridge = PygellanBridge()
magellan = bridge.get_magellan()

#create 3x3 grid centered at 0.0 stage coordinates
magellan.create_grid('New_grid', 3, 3, 0.0, 0.0)

#delete it (and anything else)
magellan.delete_all_grids_and_surfaces()