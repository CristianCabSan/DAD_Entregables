package es.us.lsi.dad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BoardListWrapper {
	private List<Board> boardList;

	public BoardListWrapper() {
		super();
	}

	public BoardListWrapper(Collection<Board> boardList) {
		super();
		this.boardList = new ArrayList<Board>(boardList);
	}
	
	public BoardListWrapper(List<Board> boardList) {
		super();
		this.boardList = new ArrayList<Board>(boardList);
	}

	public List<Board> getBoardList() {
		return boardList;
	}

	public void setBoardList(List<Board> boardList) {
		this.boardList = boardList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((boardList == null) ? 0 : boardList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BoardListWrapper other = (BoardListWrapper) obj;
		if (boardList == null) {
			if (other.boardList != null)
				return false;
		} else if (!boardList.equals(other.boardList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BoardListWrapper [boardList=" + boardList + "]";
	}

}
