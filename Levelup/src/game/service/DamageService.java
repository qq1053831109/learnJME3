package game.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.components.Damage;
import game.components.Dead;
import game.components.Health;
import game.core.Game;
import game.core.Service;

import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.es.Name;

public class DamageService implements Service {

	private Logger log = LoggerFactory.getLogger(DamageService.class);
	
	private EntityData ed;
	private EntitySet entities;

	@Override
	public void initialize(Game game) {
		ed = game.getEntityData();
		entities = ed.getEntities(Health.class, Damage.class, Name.class);
	}

	@Override
	public void update(long time) {
		if (entities.applyChanges()) {
			for (Entity e : entities) {
				String name = e.get(Name.class).getName();
				
				Damage damage = e.get(Damage.class);
				EntityId dealer = damage.getDealer();
				float delta = damage.getDelta();
				Health hp = e.get(Health.class);
				
				log.info("Dealer id="+dealer);
				
				Name dealerName = ed.getComponent(dealer, Name.class);
				if (dealerName != null) {
					log.info(dealerName.getName() + "攻击了" + name + ", 造成了" + (int)delta + "点伤害.");
				} else {
					log.info("[未知生物]攻击了" + name + ", 造成了" + (int)delta + "点伤害.");
				}
				
				// 死亡
				if (delta > hp.getCurrentHp()) {
					e.set(new Health(0, hp.getMaxHp()));
					e.set(new Dead());
				} else {
					e.set(new Health(hp.getCurrentHp() - delta, hp.getMaxHp()));
				}
				
				// 移除此伤害
				ed.removeComponent(e.getId(), Damage.class);
			}
		}
	}

	@Override
	public void terminate(Game game) {
		entities.release();
		entities = null;
	}

}
